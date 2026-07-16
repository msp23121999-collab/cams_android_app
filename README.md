# 🚀 Welcome to CAMS App! (College App Magic System)

Hello there! This is the magical folder that holds everything you need to run your very own College App! It has two main parts, like a brain and a body.

1. **The Brain (Backend & Database)**: This lives in the `backend` folder. It thinks, remembers your data, and talks to the app.
2. **The Body (Mobile App)**: This lives in the `app` folder. This is what you see on your phone!

Let's learn how to turn it all on! It is super easy!

---

## 🧠 Part 1: Wake Up the Brain (Backend)

The brain needs to wake up first so the app has someone to talk to.

### Step 1: Open the Backend
Go inside the `backend` folder.

### Step 2: Install the Magic Spells (Requirements)
We need to give the brain its tools. Open a terminal (a special typing box) and type this:
```bash
pip install -r requirements.txt
```

### Step 3: Turn it ON!
Now, start the brain by typing this:
```bash
uvicorn app.main:app --reload --host 0.0.0.0 --port 5000
```
*Yay! The brain is now awake and listening on port 5000!*

---

## 📱 Part 2: Turn on the Body (Android App)

Now we need to wake up the app on your phone or computer screen!

### Step 1: Open Android Studio
Open a program called **Android Studio** (it has a green robot logo).

### Step 2: Open this Folder
Click on "Open" and choose this exact folder that you are looking at right now (`cams-app-upload`).

### Step 3: Let it Build (Wait for the green bar)
Android Studio will do some thinking. You will see a loading bar at the bottom. Wait for it to finish!

### Step 4: Press PLAY! ▶️
At the top of Android Studio, look for a green "Play" button (like on a TV remote). Click it!
The app will now pop up on your screen or your plugged-in phone!

---

## 💾 What about the Memory (Database)?
Don't worry! The brain already comes with a memory box called `cams.db` inside the backend folder. You don't have to do anything! It remembers all the students, teachers, and homework already!

🎉 **You did it! Have fun playing with your app!** 🎉
