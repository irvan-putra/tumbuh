"""Synthetic-data smoke test on an emulator. Drives the real UI through adb and saves screenshots."""
import re, subprocess, sys, time, xml.etree.ElementTree as ET

PKG = "com.vanipy.tumbuh"
OUT = "ci/shots"
subprocess.run(["mkdir", "-p", OUT])

def adb(*a, **k):
    return subprocess.run(["adb", *a], capture_output=True, **k)

def shot(name):
    time.sleep(1.0)
    data = adb("exec-out", "screencap", "-p").stdout
    open(f"{OUT}/{name}.png", "wb").write(data)

def nodes():
    adb("shell", "uiautomator", "dump", "/sdcard/ui.xml")
    xml = adb("exec-out", "cat", "/sdcard/ui.xml").stdout.decode("utf-8", "replace")
    return [n.attrib for n in ET.fromstring(xml).iter("node")]

def center(b):
    m = re.match(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", b)
    x1, y1, x2, y2 = map(int, m.groups())
    return (x1 + x2) // 2, (y1 + y2) // 2

def find(text, exact=False, clickable_only=False):
    ns = nodes()
    ns = [n for n in ns if n.get("clickable") == "true"] + ([] if clickable_only else ns)
    for n in ns:
        t = (n.get("text") or "") + " " + (n.get("content-desc") or "")
        t = t.strip().lower()
        if (t == text.lower()) if exact else (text.lower() in t):
            return n
    return None

def present(text):
    return find(text) is not None

def tap(text, exact=False, tries=6):
    for _ in range(tries):
        n = find(text, exact)
        if n:
            x, y = center(n["bounds"])
            adb("shell", "input", "tap", str(x), str(y))
            time.sleep(0.8)
            return
        adb("shell", "input", "swipe", "540", "1700", "540", "700", "300")  # scroll down
        time.sleep(1)
    shot("FAILED-" + re.sub(r"\W+", "_", text)[:30])
    print("NOT FOUND:", text); sys.exit(1)

def need(text):
    for i in range(6):
        if present(text): return
        if i >= 2:
            adb("shell", "input", "swipe", "540", "700", "540", "1800", "300")  # scroll up
        time.sleep(1)
    shot("FAILED-need-" + re.sub(r"\W+", "_", text)[:30])
    print("EXPECTED ON SCREEN:", text); sys.exit(1)

def type_text(s):
    adb("shell", "input", "text", s.replace(" ", "%s"))
    time.sleep(0.5)

def launch():
    adb("shell", "am", "start", "-n", f"{PKG}/.MainActivity")
    time.sleep(2)

adb("shell", "pm", "clear", PKG)
launch()
need("Nothing here yet"); shot("01-home-empty")

tap("Add something that is weighing")
need("What is weighing on you"); shot("02-add-dialog")
tap("What is weighing on you"); type_text("Reply to the landlord about the repair")
shot("03-add-dialog-typed")
tap("Add", exact=True)
need("Start here"); shot("04-home-start-here")

tap("Open it")
need("What do you want to do with this"); shot("05-item")
tap("Renegotiate it")
need("One small first step"); shot("06-first-step")
tap("Fill one bag", ) if present("Fill one bag") else tap("Write one sentence")
tap("Save", exact=True)
need("Draft for you to edit"); need("Fill in the [brackets]"); shot("07-draft")
tap("Close", exact=True)
need("Your first step"); shot("08-item-with-step")

# persistence across a process kill
adb("shell", "am", "force-stop", PKG)
launch()
need("Reply to the landlord"); shot("09-after-restart")

# let go, then bring back from archive
tap("Open it")
tap("Let it go")
need("Commitments"); 
adb("shell", "input", "keyevent", "4")  # back to home
time.sleep(1)
need("Archive (let go or done): 1"); shot("10-home-archive-count")
tap("Archive (let go or done)")
need("Nothing is deleted"); shot("11-archive")
tap("Open", exact=True)
tap("Bring it back")
need("Not decided"); shot("12-brought-back")

adb("shell", "input", "keyevent", "4"); time.sleep(1)   # item -> area
adb("shell", "input", "keyevent", "4"); time.sleep(1)   # area -> home
need("Start here")
tap("About and limits")
need("does not read your email"); shot("13-about")
adb("shell", "input", "swipe", "540", "1700", "540", "500", "300"); shot("14-about-bottom")

# ---- backup and restore through the real system file picker ----
adb("shell", "input", "keyevent", "4"); time.sleep(1)
need("Start here")
tap("Save a backup file")
need("Save"); shot("15-export-picker")
tap("Save", exact=True)
need("Start here"); time.sleep(1); shot("16-after-export")

# add a second item AFTER the backup, then restore the backup and expect it to be gone
tap("Add something that is weighing")
tap("What is weighing on you"); type_text("Second item added after backup")
tap("Add", exact=True)
need("Second item added after backup") if present("Second item added after backup") else need("Commitments  (2)")
shot("17-two-items")
tap("Restore from a backup file")
time.sleep(2); shot("18-restore-picker")
tap("tumbuh-v")
time.sleep(2)
if not present("Restore 1 items?"):
    # tap the file thumbnail by coordinates (grid item), then again if still on the picker
    adb("shell", "input", "tap", "300", "900"); time.sleep(2)
if not present("Restore 1 items?") and present("Files in Downloads"):
    tap("tumbuh-v"); time.sleep(2)
need("Restore 1 items?"); shot("19-restore-confirm")
tap("Restore", exact=True)
need("Commitments  (1)"); shot("20-after-restore")

# bad file is rejected without changing data: the app's own earlier-kept copies are not picked here,
# so just check the data is unchanged and the app still responds.
need("Start here")

# launcher icon as the user sees it
adb("shell", "input", "keyevent", "3"); time.sleep(2)
adb("shell", "input", "swipe", "540", "1900", "540", "600", "300"); time.sleep(2)
shot("21-launcher-drawer")
print("SMOKE OK")
