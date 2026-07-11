r"""
app.py - Entry point for CAMS Backend
Run from the backend/ directory with ANY of these:
  py app.py
  .\.venv\Scripts\python.exe app.py
  python app.py

If run outside the venv, this script auto-relaunches using the venv Python.
"""
import sys
import os
import subprocess

# ── Venv auto-detection & re-launch ────────────────────────────────────────────
_BACKEND_DIR = os.path.dirname(os.path.abspath(__file__))
_VENV_PYTHON  = os.path.join(_BACKEND_DIR, ".venv", "Scripts", "python.exe")   # Windows
_VENV_PYTHON3 = os.path.join(_BACKEND_DIR, ".venv", "bin",     "python3")      # Linux/Mac

def _in_venv() -> bool:
    """Return True if the current interpreter is inside our .venv."""
    return (
        hasattr(sys, "real_prefix")                       # virtualenv
        or (                                               # venv
            hasattr(sys, "base_prefix")
            and sys.base_prefix != sys.prefix
        )
    )

if not _in_venv():
    # Pick the right venv Python for the OS
    venv_py = _VENV_PYTHON if os.path.exists(_VENV_PYTHON) else _VENV_PYTHON3
    if os.path.exists(venv_py):
        print(f"[app.py] Detected system Python — re-launching with venv: {venv_py}")
        result = subprocess.run([venv_py, __file__] + sys.argv[1:])
        sys.exit(result.returncode)
    else:
        print("[app.py] WARNING: .venv not found — running with system Python (may fail)")

# ── Normal startup ──────────────────────────────────────────────────────────────
sys.path.insert(0, _BACKEND_DIR)

import uvicorn

if __name__ == "__main__":
    uvicorn.run(
        "app.main:app",
        host="0.0.0.0",
        port=8000,
        reload=True,
        reload_dirs=["app"],
        log_level="info",
    )
