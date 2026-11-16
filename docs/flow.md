# Smooth Swapping – how it works

Program flow from the start of a frame to the drawn item.
Boxes with double side lines are subroutines with their own chart below.

## Main flow (once per frame)

```mermaid
flowchart TD
    start(["Frame starts<br/>GameRenderer.render (before 26)<br/>GameRenderer.extract (26+)"])
    isContainer{"Current screen is an<br/>AbstractContainerScreen?"}
    beginFrame["SwapRenderer.beginFrame()<br/>clear the legacy deferred list"]
    enabled{"Mod enabled and<br/>not the creative inventory?"}
    resetOff["Clear all swaps<br/>forget the snapshot"]
    frameTime["frameTime = Util.getMillis()<br/>one clock for the whole frame"]
    removeFinished["Remove finished swaps"]
    mouse["Mouse position in slot space<br/>mouse - leftPos - 8, mouse - topPos - 8"]
    detect[["Detection<br/>SwapDetector.detect"]]
    error{"Exception?"}
    resetError["Log once, clear all swaps<br/>forget the snapshot"]
    screen["The screen draws itself<br/>vanilla or a mod's own render"]
    slots[["Slot rendering<br/>once per drawn slot"]]
    flush[["Flush deferred flying items"]]
    cursor[["Cursor rendering<br/>renderFloatingItem / extractFloatingItem"]]
    finish(["Frame done"])

    start --> isContainer
    isContainer -- no --> finish
    isContainer -- yes --> beginFrame
    beginFrame --> enabled
    enabled -- no --> resetOff --> finish
    enabled -- yes --> frameTime --> removeFinished --> mouse --> detect
    detect --> error
    error -- yes --> resetError --> screen
    error -- no --> screen
    screen --> slots --> flush --> cursor --> finish
```

Notes:

- Detection runs **before** anything is drawn, so every hook in this frame sees the same swaps and the same `frameTime`.
- Detection starts from `GameRenderer`, not from the screen, so it also runs for screens that override `render` without calling `super` (e.g. Sophisticated).
- The order slots → flush → cursor is what keeps flying items above every slot but below the carried item.
