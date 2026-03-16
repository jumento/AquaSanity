# Aqua-Sanity 🧠
> *A psychological survival and sanity mod for Hytale.*

**Aqua-Sanity** introduces a dynamic, hidden psychological survival mechanic to Hytale. It silently tracks the player's mental state based on their actions, environment, and combat encounters. When sanity drops, the game pushes back with unpredictable hallucinations, fake events, and sudden scares.

## ✨ Features
* **Dynamic Sanity Core:** Tracks light levels, combat status, stamina, and rest to calculate sanity in real-time.
* **Madness Event Matrix:** 7 unique randomized events triggered at low sanity thresholds (Jumpscares, Fake Chat Messages, Teleportation, Camera Shakes, Status Effects, Hostile Spawns, and Phantom Damage).
* **Audio Subsystem:** Over 80+ integrated hallucination sound effects.
* **Skill Tree Integration:** Fully connected with Hytale's Illumination skill tree multipliers.
* **In-Game Configuration:** Fully customizable via the `/AquaSanityconfig` command.

## 🛠️ Building from Source
This project uses Gradle for its build lifecycle. To compile the mod locally, follow these steps:

1. Clone the repository:
   ```bash
   git clone [https://github.com/your-username/aqua-sanity.git](https://github.com/your-username/aqua-sanity.git)
Navigate to the project directory:

Bash
cd aqua-sanity
Build the project using the Gradle wrapper:

Bash
# On Windows
gradlew build

# On macOS/Linux
./gradlew build
The compiled .jar (or executable mod file) will be located in the build/libs/ directory.

🎮 How it Works (Mechanics)
Sanity is influenced passively by the environment and player actions:

Gaining Sanity: Resting in beds, sitting on chairs, staying in well-lit areas, and feeding passive animals.

Losing Sanity: Staying in darkness (light level ≤ 5), taking damage, depleting stamina, or being targeted by hostile mobs.

When sanity drops to ≤ 35, the Madness Events begin. At 0 sanity, the player is afflicted with the Insanity status effect, significantly reducing speed and dealing continuous, armor-piercing damage.

📝 License
[Specify your license here, e.g., MIT, GPL-3.0, or All Rights Reserved]

Created by jume.
