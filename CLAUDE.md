# CollisionBirds - LibGDX

## Project Info
- **Original Engine**: AndEngine GLES2 AnchorCenter (discontinued)
- **Target Engine**: LibGDX 1.14.0
- **Package**: br.com.tupinikimtecnologia
- **Game Resolution**: 800x480 (landscape)

## Paths
- **LibGDX Project**: /home/felipe/Development/Projects/IdeaProjects/CollisionBirds
- **Original AndEngine Project**: /home/felipe/Development/Projects/Bkp/CollisionBirds/CollisionBirds
- **AndEngine Root (with libs)**: /home/felipe/Development/Projects/Bkp/CollisionBirds

## Build & Run
- Desktop: `./gradlew lwjgl3:run`
- Android: `./gradlew android:run`
- HTML/GWT: `./gradlew html:superDev` (note: FreeType fonts not supported on GWT)

## Architecture
All source in `core/src/main/java/br/com/tupinikimtecnologia/`:
- `Main.java` - Game entry point (extends Game), owns shared SpriteBatch and Assets
- `config/GameConfig.java` - Game constants (resolution, timers, velocities)
- `manager/Assets.java` - Centralized asset loading (textures via FreeType, fonts)
- `entity/BirdType.java` - Bird type enum with health and spawn probability ranges
- `entity/BirdEntity.java` - Enemy bird with 4-direction animation, wall bouncing, health system
- `entity/PlayerEntity.java` - Player bird (touch-drag controlled, front animation)
- `screen/SplashScreen.java` - Splash screen (2 seconds, green background)
- `screen/LoadingScreen.java` - Brief "Loading..." transition between screens
- `screen/MenuScreen.java` - Main menu with play/rank/rate buttons, Back/Escape exits
- `screen/GameScreen.java` - Core gameplay: bird spawning, collision detection, game over overlay

## Game Mechanics
- Player controls a blue bird by touch-dragging (must touch ON the player first)
- Enemy birds spawn every 5 seconds with random types
- Birds bounce off walls (losing 1 health per bounce) and off each other
- Collision with an active (non-immortal, alive) enemy bird triggers game over
- Score is time survived (minutes.seconds format)
- 4 bird types: Red (hp=5, 14%), Green (hp=60, 47%), Yellow (hp=150, 11%), Purple (hp=15, 28%)
- New birds are immortal (transparent) for 3 seconds after spawn
- Birds with 0 health show death frame, become transparent, are removed after 4 seconds
- Back/Escape key: returns to menu from game, exits app from menu

## Technical Notes
- **Velocity conversion**: Original AndEngine used Box2D m/s with PPM=32. LibGDX uses px/s (multiplied by 32)
- **FreeType**: Used for "8-BIT WONDER.TTF" and "Square.ttf" fonts (desktop/Android only, not GWT)
- **No Box2D**: Physics are manually implemented (velocity integration, AABB collision, separation)
- **Sprite sheets**: 4x4 grids. Row 0=front, Row 1=left, Row 2=right, Row 3=death frame
- **Coordinate system**: 800x480, (0,0) at bottom-left, FitViewport for aspect ratio
- **Shared SpriteBatch**: Single batch in Main, reused by all screens
