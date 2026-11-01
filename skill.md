# Role & Project Context

* **Project Name:** Secure da Tower

* **Developer:** Computer Science Student

* **Core Language & Tech Stack:** **Java** (Standard OOP conventions, Swing/JavaFX/Canvas graphics depending on project setup)

* **Core Paradigm:** Object-Oriented Programming (OOP)

* **Core Gameplay:** The player controls a central turret, using the mouse to aim and left-click to shoot incoming enemies from surrounding directions. The goal is to defend the turret and prevent its HP from reaching zero.

# Requirements & Instructor Feedback

The project proposal has been approved with specific mandatory updates requested by the instructor:

1. **Multiple Levels:** Implement more than one level (e.g., Level 1, Level 2, Level 3). Each level must feature a unique map layout and distinct obstacle themes/mechanics.

2. **More Obstacles:** Significantly increase the presence of obstacles in each level. Include both static obstacles (e.g., barriers blocking bullet trajectory) and dynamic/moving obstacles.

# Class & Game Structure Guidelines (Java Implementation)

* **Game / Level Manager:** Manages level transitions, map loading, game state updates, and Win/Lose condition checks.

* **Tower:** Handles turret behavior (rotating toward mouse cursor, firing bullets, managing health/damage).

* **Bullet:** Handles projectile mechanics (calculating trajectories, detecting collisions with enemies and obstacles).

* **Enemy (Base & Subclasses):** Defines enemy behaviors (e.g., ground vs. air, fast vs. heavy variants).

* **Obstacle (Base & Subclasses):** Defines environmental barriers (e.g., static cover, moving hazards).

# AI Behavioral Rules & Operational Constraints

### 1. Language & Code Style Constraint

* **All implementation code MUST be strictly written in Java.**

* Follow standard Java naming conventions (PascalCase for classes, camelCase for methods and variables, UPPER_CASE for constants).

### 2. Strict Scope & No Unsolicited Actions

* **Execute ONLY explicit instructions.** Do not assume features, invent unasked requirements, or add unnecessary code refactoring.

* If you identify potential improvements or bug risks outside the requested scope, **suggest them briefly as text** and wait for explicit permission before implementing.

### 3. Confirm Before Action

* **Always confirm your technical approach** with the user before generating or modifying Java code.

* Provide a short overview explaining *what* will be modified and *why*, then wait for user confirmation to proceed.

### 4. Minimal & Targeted Code Changes

* Modify only the exact components required to fulfill the specific task. **Do not rewrite, refactor, or reorganize** existing functional Java code unless explicitly requested.

* Provide code snippets focused narrowly on the changed methods or lines rather than dumping entire Java source files unnecessarily.

### 5. Step-by-Step Implementation

* Break complex features into small, incremental milestones (e.g., Step 1: Define Java `Obstacle` class -> Step 2: Implement Collision Logic -> Step 3: Integrate Level Switcher).

* Pause at each milestone and wait for user testing feedback before proceeding to the next step.