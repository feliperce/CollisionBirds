# CollisionBirds Decompiled APK - Complete Analysis

## FILE INVENTORY (61 Java files total)

---

## PART 1: ROOT PACKAGE FILES

### GameActivity.java -- MAIN ACTIVITY
**Deobfuscated name: GameActivity** (not obfuscated)

This is the AndEngine-based game activity. Key details:

- **Resolution**: 800x480, LANDSCAPE_FIXED orientation, 60 FPS
- **Camera**: BoundCamera at (0,0,800,480)
- **AdMob banner ad** with unit ID `ca-app-pub-5890786745182778/7171286444`
- **Google Analytics** tracker: `UA-50818031-1`

**Fields:**
- `a` (static f/AdView) -> `adView`
- `b` (HashMap) -> `trackerMap`
- `h` (Engine) -> `engine` (andengine)
- `i` (d.a) -> `resourceManager` - the singleton ResourceManager
- `j` (int) -> `musicStateOnPause` - tracks which music was playing when paused (1=menu, 2=game, 3=gameover)
- `k` (Tracker) -> `appTracker`

**Key methods:**
- `a(boolean, Activity)` -> `showAd(visible, activity)` - shows/hides the banner ad on the UI thread
- `a(c)` -> `onCreateEngineOptions()` - configures 800x480, landscape, screen on
- `a(e,d)` -> `onCreateResources()` - registers a 2-second splash delay, then calls `SceneManager.d()` to transition to menu
- `a(b)` -> `onCreateScene()` - loads resources via `ResourceManager.a(engine, activity, camera, vbom)`, then calls `SceneManager.a(splash)`
- `onKeyDown` -> BACK key calls `SceneManager.a().c().b()` which calls the current scene's `b()` method (back/exit)
- `onPause` -> stops whichever music is playing (r=menu, s=game, t=gameover), records which one
- `onResume` -> resumes the music that was paused

### a.java (root) -- SPLASH TRANSITION CALLBACK
**Deobfuscated: SplashTransitionCallback**
- Registered as callback after splash timer
- Removes splash from engine, then calls `SceneManager.d()` to load menu

### b.java (root) -- AD LISTENER
**Deobfuscated: AdLoadListener**
- `onAdLoaded()` -> runs `c` (Runnable) on UI thread that hides the ad (setVisibility(GONE))

### c.java (root) -- AD HIDE RUNNABLE
**Deobfuscated: AdHideRunnable**
- Hides the AdView when ad loads

### d.java (root) -- AD SHOW/HIDE RUNNABLE
**Deobfuscated: AdVisibilityRunnable**
- Constructor takes boolean `show`
- If true: setVisibility(VISIBLE), refreshDrawableState, bringToFront
- If false: setVisibility(GONE)

### e.java (root) -- TRACKER ENUM
**Deobfuscated: TrackerType**
```
APP_TRACKER, GLOBAL_TRACKER, ECOMMERCE_TRACKER
```

### R.java -- ANDROID RESOURCES
Key resource IDs:
- Achievements: meet_the_team, first_blood, you_are_too_slow, wooow_amazing, you_are_the_rambo, what_is_that, very_fast, 20_minutes, yellow_bird_killer, medieval_soldier, elite_medieval_soldier, potion_of_invisibility, vicious_potion, bird_spanker, bird_homicide, killer_jr, 1000_birds
- Leaderboards: leaderboard_time, leaderboard_birds_killed
- Dialog strings: dialog_rank_titulo, dialog_rank_msg, dialog_rank_button_rank_time, dialog_rank_button_rank_kills

---

## PART 2: PACKAGE a/ -- BASE SCENE

### a/a.java -- ABSTRACT BASE SCENE
**Deobfuscated: BaseScene**

All scenes extend this. Fields:
- `a` (Engine) -> `engine`
- `b` (Activity) -> `activity`
- `c` (d.a / ResourceManager) -> `resourceManager`
- `d` (VertexBufferObjectManager) -> `vbom`
- `e` (Camera) -> `camera`

Abstract methods:
- `a()` -> `createScene()` - build the scene
- `b()` -> `onBackPressed()` - handle back button
- `c()` -> `getSceneType()` - returns the scene enum type
- `d()` -> `disposeScene()` - cleanup

---

## PART 3: PACKAGE b/ -- GAME FLAGS

### b/a.java -- ACHIEVEMENT/STATE FLAGS
**Deobfuscated: GameFlags**

All static fields, used to track achievements unlocked in the current session:
- `a` (boolean) -> `flag_a` (unused/unknown)
- `b` (boolean) -> `firstGameOverShown` - whether first game-over interstitial was shown
- `c` (int = 5) -> `interstitialAdFrequency` - random chance denominator for showing interstitial
- `d` (boolean) -> `achievementMeetTheTeam` - "Meet the Team" (visit about screen)
- `e` (boolean) -> `achievementFirstBlood` - "First Blood" (die for the first time)
- `f` (boolean) -> `achievementTooSlow` - ">20 seconds survived"
- `g` (boolean) -> `achievementAmazing` - ">2 minutes survived"
- `h` (boolean) -> `achievementVeryFast` - ">5 minutes survived"
- `i` through `j` -> various unused flags
- `k` (boolean) -> `achievement20Minutes` - ">20 minutes survived"
- `l` (boolean) -> `giftizMission1Min` - survived 1 minute (giftiz related)
- `m` through `q` -> unused
- `r` (boolean) -> `achievementBirdSpanker` - killed >=15 birds
- `s` (boolean) -> `achievementBirdHomicide` - killed >=30 birds
- `t` (boolean) -> `achievementKillerJr` - killed >=10 birds
- `u` (boolean) -> `achievement1000Birds` - killed >=200 birds (incorrectly mapped to "too slow" achievement string)

---

## PART 4: PACKAGE c/a/ -- GOOGLE PLAY GAME SERVICES HELPER

### c/a/a.java -- BASE GAME ACTIVITY
**Deobfuscated: BaseGameActivity**
- Extends AndEngine's SimpleBaseGameActivity
- Integrates GameHelper for Google Play sign-in
- GameActivity extends this class

### c/a/b.java -- GAME HELPER
**Deobfuscated: GameHelper**
- Full Google Play Games sign-in implementation
- Handles connection, disconnection, sign-in failures
- Manages SharedPreferences for sign-in cancellation count
- Max attempts before giving up: 3

### c/a/c.java -- SIGN IN FAILURE RUNNABLE
### c/a/d.java -- SIGN IN LISTENER INTERFACE
Methods: `d()` = onSignInFailed, `e()` = onSignInSucceeded

### c/a/e.java -- SIGN IN FAILURE REASON
Holds error code and activity result code.

### c/a/f.java -- GAME HELPER UTILS
Error code to string conversions, SHA1 fingerprint, app ID retrieval.

---

## PART 5: PACKAGE d/ -- RESOURCE MANAGER & SCENE MANAGER

### d/a.java -- RESOURCE MANAGER (SINGLETON)
**Deobfuscated: ResourceManager**

**CRITICAL FILE** - loads all textures, sounds, fonts.

**Singleton**: `M` is the instance, `h()` returns it.

**Texture regions (game assets):**
- `e` (TextureRegion) -> `splashTextureRegion` - splash.png
- `f` (TextureRegion) -> `menuBackgroundRegion` - gfx/menu/background.png
- `g` (TextureRegion) -> `gameBackgroundRegion` - gfx/game/background-game.png (also reused for about bg)
- `h` (TextureRegion) -> `buttonPlayRegion` - button_play.png
- `i` (TextureRegion) -> `buttonRateRegion` - button_rate.png
- `j` (TextureRegion) -> `buttonRankRegion` - button_rank.png
- `k` (TextureRegion) -> `buttonAchievementsRegion` - button_achievements.png
- `l` (TextureRegion) -> `buttonAboutRegion` - button_about.png
- `m` (TextureRegion) -> `aboutImageRegion` - gfx/sobre/about.png
- `n` (TextureRegion) -> `logoRegion` - logo_cbirds.png
- `o` (TextureRegion) -> `copyrightRegion` - copyright_tupinikim.png

**Game texture regions:**
- `B` (TextureAtlas) -> `gameTextureAtlas` (1024x1024)
- `C` (TiledTextureRegion) -> `birdYellowRegion` - birdAmarelo.png (4x4 tiles)
- `D` (TiledTextureRegion) -> `birdRedRegion` - birdVermelho.png (4x4 tiles)
- `E` (TiledTextureRegion) -> `birdGreenRegion` - birdVerde.png (4x4 tiles)
- `F` (TiledTextureRegion) -> `birdVioletRegion` - birdVioleta.png (4x4 tiles)
- `G` (TiledTextureRegion) -> `lifeStatRegion` - life-stat.png (4x1 tiles) -- the 1-UP item
- `H` (TextureRegion) -> `shieldStatRegion` - shield-stat.png -- shield item sprite
- `I` (TextureRegion) -> `potionInvStatRegion` - potioninv-stat.png -- potion item sprite
- `J` (TextureRegion) -> `lifeIconRegion` - life.png -- HUD life icon
- `K` (TiledTextureRegion) -> `playerBirdRegion` - birdPlayer.png (4x4 tiles)
- `L` (TextureRegion) -> `gameOverPopupRegion` - popup_gameover.png

**Fonts:**
- `p` (Font) -> `hudFont` - "8-BIT WONDER.TTF", size 30 for game HUD (white with black stroke)
- `q` (Font) -> `scoreFont` - "Square.ttf", size 40 for game over score (black)

**Music:**
- `r` (Music) -> `menuMusic` - mfx/menu-music.ogg (loops)
- `s` (Music) -> `gameMusic` - mfx/game-music.ogg (loops)
- `t` (Music) -> `gameOverMusic` - mfx/gameover-music.ogg (loops, volume 2.0)

**Sound effects:**
- `u` (Sound) -> `menuClickSound` - sfx/menu-click.ogg
- `v` (Sound) -> `playerDieSound` - sfx/player-die.ogg
- `w` (Sound) -> `punchSound` - sfx/punch.ogg
- `x` (Sound) -> `oneUpSound` - sfx/1up.ogg
- `y` (Sound) -> `potionUseSound` - sfx/potioninv-use.ogg
- `z` (Sound) -> `shieldUseSound` - sfx/shield-use.ogg
- `A` (Sound) -> `shieldLostSound` - sfx/shield-lost.ogg

**Load methods:**
- `a()` -> `loadMenuResources()` - calls m() + e() + l()
- `b()` -> `loadGameResources()` - calls o() + c() + q() + r()
- `c()` -> `loadGameOverResources()` - calls n()
- `d()` -> `loadAboutResources()` - calls p()
- `e()` -> `loadMenuMusicResources()`
- `f()` -> `loadSplashResources()` - splash.png

### d/b.java -- SCENE MANAGER (SINGLETON)
**Deobfuscated: SceneManager**

**Singleton**: `f` is the instance, `a()` returns it.

**Fields:**
- `a` (BaseScene) -> `splashScene`
- `b` (BaseScene) -> `menuScene`
- `c` (BaseScene) -> `gameScene`
- `d` (BaseScene) -> `aboutScene`
- `e` (BaseScene) -> `loadingScene`
- `g` (SceneType enum) -> `currentSceneType`
- `h` (BaseScene) -> `currentScene`
- `i` (Engine) -> `engine`

**Key methods:**
- `a(BaseScene)` -> `setScene(scene)` - sets engine scene, updates currentScene and sceneType
- `a(Engine)` -> `loadGameScene(engine)` - transitions from menu to game with 0.1s fade, loads game resources
- `b(Engine)` -> `loadMenuScene(engine)` - transitions from game/about back to menu with 0.1s fade
- `c(Engine)` -> `loadAboutScene(engine)` - transitions to about screen
- `d()` -> `initMenuFromSplash()` - loads menu resources, creates MenuScene + LoadingScene, sets menu, disposes splash
- `c()` -> `getCurrentScene()`
- `b()` -> `getCurrentSceneType()`

### d/c.java -- GAME SCENE LOAD TRANSITION
Callback after fade to game scene. Creates `new e()` (GameScene) and sets it.

### d/d.java -- MENU SCENE LOAD TRANSITION
Callback after fade to menu. Reloads menu textures and sets menu scene.

### d/e.java -- ABOUT SCENE LOAD TRANSITION
Callback after fade to about. Loads about resources, creates `new a()` (AboutScene), sets it.

### d/f.java -- SCENE TYPE ENUM
```
SCENE_SPLASH, SCENE_MENU, SCENE_GAME, SCENE_ABOUT, SCENE_LOADING
```

---

## PART 6: PACKAGE e/ -- GAME ENTITIES

### e/a.java -- BIRD ATTRIBUTES
**Deobfuscated: BirdAttributes**

Determines bird behavior based on type:

**Fields:**
- `a` (int) -> `bounceCount` - how many wall bounces before bird disappears
- `b` (float) -> `speed` - movement speed
- `c` (int) -> `rangeStart` - random range start (used in spawn probability)
- `d` (int) -> `rangeEnd` - random range end
- `e` (BirdType) -> `birdType`

**Bird type -> attributes mapping:**
| Bird Type | Bounce Count | Speed |
|-----------|-------------|-------|
| PASSARO_VERDE (Green) | 35 | 10.0 |
| PASSARO_VERMELHO (Red) | 6 | 30.0 |
| PASSARO_AMARELO (Yellow) | 130 | 10.0 |
| PASSARO_VIOLETA (Violet) | 15 | 15.0 |

### e/b.java -- BIRD TYPE ENUM
**Deobfuscated: BirdType**
```
PASSARO_VERDE,     // Green bird
PASSARO_VERMELHO,  // Red bird
PASSARO_AMARELO,   // Yellow bird
PASSARO_VIOLETA    // Violet bird
```

### e/c.java -- BIRD ENTITY (ENEMY)
**Deobfuscated: BirdEnemy**

Extends AnimatedSprite. This is the enemy bird entity with physics.

**Fields:**
- `a` (PhysicsConnector) -> `physicsConnector`
- `b` (Body) -> `physicsBody`
- `c` (static FixtureDef) -> `FIXTURE_DEF` (density=0, elasticity=1.0, friction=0)
- `m` (int) -> `remainingBounces` - decrements on wall hits
- `n` (float) -> `speed`
- `o` (boolean = true) -> `isInvisible` - starts invisible
- `p` (boolean = true) -> `needsInitialSetup` - first frame flag
- `q` (boolean = false) -> `isMarkedForRemoval`
- `r` (boolean = false) -> `hasBeenHit` - marked as killed/hit by player
- `s` (BirdType) -> `birdType`

**Key methods:**
- `a(float)` -> `onManagedUpdate(dt)`:
  - If `remainingBounces == 0`: stop animation, set frame 12, mark invisible, deactivate physics, set alpha 0.5
  - If `needsInitialSetup`: if invisible, set alpha 0.5 and deactivate; else set alpha 1.0, activate physics, clear flag
  - **Wall bouncing logic**:
    - If X position < half width (left wall): decrement bounces, reverse X velocity to positive (`+speed`), play animation frames 8-11 (facing right)
    - If X position + half width > 800 (right wall): decrement bounces, reverse X velocity to negative (`-speed`), play animation frames 4-7 (facing left)
    - If Y position < half height (top wall): decrement bounces, reverse Y velocity to positive (`+speed`)
    - If Y position + half height > 480 (bottom wall): decrement bounces, reverse Y velocity to negative (`-speed`), play animation frames 0-3 (facing down)

- `a(PhysicsWorld)` -> `createBody(physicsWorld)`:
  - Creates DynamicBody with the FIXTURE_DEF
  - Sets userData to `this`
  - Registers PhysicsConnector
  - **Initial velocity**: `(-speed, 10.0f)` -- birds always start moving LEFT and slightly DOWN

- `a(int)` -> `setRemainingBounces(count)`
- `a(boolean)` -> `setInvisible(invisible)`
- `b()` -> `isInvisible()`
- `b(boolean)` -> `setMarkedForRemoval(removed)`
- `c()` -> `getRemainingBounces()`
- `c(boolean)` -> `setHasBeenHit(hit)`
- `d()` -> `isMarkedForRemoval()`
- `e()` -> `hasBeenHit()`
- `f()` -> `getBirdType()`

### e/d.java -- 1-UP ITEM SPRITE
**Deobfuscated: OneUpItem**

Simple AnimatedSprite, no special logic. Created with `life-stat.png` (4x1 tiled).

### e/e.java -- PLAYER ENTITY
**Deobfuscated: PlayerBird**

Extends AnimatedSprite. This is the player's bird.

**Fields:**
- `a` (boolean = false) -> `isDead`
- `b` (boolean = false) -> `hasPotionInvisibility` - potion/invincibility active
- `c` (boolean = false) -> `hasShield` - shield active
- `m` (int) -> `lives`

**Key methods:**
- `a(int)` -> `setLives(count)`
- `a(boolean)` -> `setDead(dead)`
- `a()` -> `isDead()`
- `b(boolean)` -> `setPotionInvisibility(active)`
- `b()` -> `hasPotionInvisibility()`
- `c(boolean)` -> `setShield(active)`
- `c()` -> `hasShield()`
- `d()` -> `getLives()`

---

## PART 7: PACKAGE f/ -- ALL SCENES AND GAME LOGIC

### f/e.java -- THE GAME SCENE (CRITICAL - LARGEST FILE)
**Deobfuscated: GameScene**

This is the main game scene. Extends BaseScene.

#### STATIC FIELDS
- `f` (static Bird) -> `currentSpawningBird` - temporary reference during spawn
- `g` (static ArrayList) -> `birdList` - list of all active enemy birds
- `h` (static PlayerBird) -> `player` - the player entity

#### INSTANCE FIELDS
- `aA` (SharedPreferences) -> `highScorePrefs` (key "trash")
- `aB` (SharedPreferences.Editor) -> `highScoreEditor`
- `aC` (boolean) -> `isGameStopped` - true when game over, stops spawning/collision
- `aD` (boolean) -> `cancelBirdAppearTimer` - cancellation flag for bird appear animation timer
- `aE` (boolean) -> `cancelBirdMarkRemoveTimer` - cancellation flag for bird mark-removable timer
- `aF` (boolean) -> `cancelTimeTimer` - cancellation flag for time counter
- `aG` (boolean) -> `cancelShieldRemoveTimer` - cancellation flag for shield item removal
- `aH` (boolean) -> `cancel1UpRemoveTimer` - cancellation flag for 1-UP item removal
- `aI` (boolean) -> `cancelShieldExpireTimer` - cancellation flag for shield duration expiry
- `aJ` (boolean) -> `cancelPotionExpireTimer` - cancellation flag for potion expiry (sprite removal)
- `aK` (boolean) -> `cancelPotionEffectExpireTimer` - cancellation flag for potion effect expiry
- `aL` (boolean) -> `cancelInvincibilityExpireTimer` - cancellation flag for invincibility after hit
- `aM` (ButtonMenu) -> `gameOverMenu`
- `aN` (final int = 0) -> `MENU_PLAY`
- `aO` (final int = 1) -> `MENU_RANK`
- `aP` (int) -> `birdsKilledCount` - total birds killed this game
- `aQ` (int) -> `yellowBirdsKilledCount`
- `aR` (int) -> `potionsUsedCount`
- `aS` (int) -> `shieldsUsedCount`
- `aT` (AlertDialog) -> `rankDialog`
- `aU` (InterstitialAd) -> `interstitialAd`
- `ah` (HUD) -> `hud`
- `ai` (Text) -> `timeLabelText` - "Time:" label
- `aj` (Text) -> `timeValueText` - the time counter display
- `ak` (Text) -> `gameOverScoreText` - score on game over popup
- `al` (Text) -> `gameOverTimeText` - time record on game over popup
- `am` (Text) -> `livesText` - "x 5" lives counter
- `an` (String) -> `timeRecordString` - formatted time record
- `ao` (long) -> `minutes` - elapsed minutes
- `ap` (long) -> `seconds` - elapsed seconds (actually frames, 0-59 per minute tick)
- `aq` (long) -> `currentTimeMillis` - current game time in milliseconds
- `ar` (long) -> `highScoreMillis` - stored high score
- `as` (PhysicsWorld) -> `physicsWorld`
- `at` (Sprite) -> `shieldItemSprite` - the shield pickup on screen
- `au` (Sprite) -> `potionItemSprite` - the potion pickup on screen
- `av` (boolean) -> `shieldItemOnScreen` - whether shield item is currently spawned
- `aw` (boolean) -> `potionItemOnScreen` - whether potion item is currently spawned
- `ax` (boolean) -> `oneUpItemOnScreen` - whether 1-UP item is currently spawned
- `ay` (boolean = false) -> `isGameOver` - set true when game over sequence runs
- `az` (GameOverPopup) -> `gameOverPopup`
- `i` (Sprite) -> `lifeIconSprite` - the life icon in HUD
- `j` (OneUpItem) -> `oneUpItemEntity` - the 1-UP item on screen
- Timers `k` through `t` -> various TimerHandlers (see below)

#### TIMER HANDLERS (the letter-named TimerHandler fields)
- `k` -> `birdSpawnTimer` (5.0s, repeating) - spawns a new bird every 5 seconds
- `l` -> `birdAppearTimer` (3.0s, one-shot) - makes bird visible 3s after spawn
- `m` -> `birdMarkRemovableTimer` (4.0s, one-shot) - marks bird removable 4s after becoming visible
- `n` -> `timeCounterTimer` (1.0s, repeating) - increments time display every second
- `o` -> `potionSpriteRemoveTimer` (6.0s, one-shot) - removes potion item from screen
- `p` -> `shieldSpriteRemoveTimer` (6.0s, one-shot) - removes shield item from screen
- `q` -> `oneUpSpriteRemoveTimer` (6.0s, one-shot) - removes 1-UP item from screen
- `r` -> `shieldDurationTimer` (6.0s, one-shot) - shield effect expires after 6s
- `s` -> `invincibilityDurationTimer` (4.0s, one-shot) - invincibility after hit expires after 4s
- `t` -> `potionDurationTimer` (13.0s, one-shot) - potion effect expires after 13s

#### CRITICAL METHODS

**`a()` -> `createScene()`:**
1. Hides ads: `GameActivity.showAd(false)`
2. Initializes `birdList = new ArrayList()`
3. Creates background sprite at (400, 240)
4. Creates player at (400, 240) with 5 lives: `a(400, 240, 5)`
5. Creates HUD: `aq()`
6. Starts time counter: `h()`
7. Creates physics world: `ar()`
8. Starts bird spawn timer: `aE()` - 5 second interval
9. Registers update handler: `aA()` - game loop
10. Registers player as touch handler
11. Sets multitouch support: `c(true)`
12. Starts game music: `resourceManager.gameMusic.play()`

**`a(float, float, int)` -> `createPlayer(x, y, lives)`:**
- Creates `PlayerBird` (class v, which is player subclass) at given position
- Uses `birdPlayer.png` texture (4x4 tiles)
- Starts animation: frames 0-3, 200ms per frame, looping
- Attaches to scene
- Player is touch-draggable (see class v)

**`o(float, float)` -> `spawnBird(x, y)`:**
BIRD SPAWN PROBABILITIES (random 0-99):
- 0-13 (14%): RED bird (PASSARO_VERMELHO) - BirdAttributes(0,13), bounces=6, speed=30
- 14-60 (47%): GREEN bird (PASSARO_VERDE) - BirdAttributes(14,60), bounces=35, speed=10
- 61-71 (11%): YELLOW bird (PASSARO_AMARELO) - BirdAttributes(61,71), bounces=130, speed=10
- 72-100 (29%): VIOLET bird (PASSARO_VIOLETA) - BirdAttributes(72,100), bounces=15, speed=15

After creation:
1. Animates frames 0-3, looping
2. Sets invisible (true) - bird starts invisible
3. Creates physics body
4. Adds to bird list
5. Attaches to scene
6. Calls `a(bird)` to schedule appear timer (3s) then mark-removable timer (4s after appear)

**`aE()` -> `startBirdSpawnTimer()`:**
- Creates repeating timer, 5.0 seconds
- Callback `w`: spawns bird at random position (x: 30-770, y: 30-450) if game not stopped

**`h()` -> `startTimeCounter()`:**
- 1-second repeating timer (callback `g`)
- Increments `seconds` (0-59), when reaches 60: increments `minutes`, resets seconds
- Also handles item spawning every tick (see below)

**`a(bird)` -> `scheduleBirdAppear(bird)`:**
- One-shot 3.0s timer (callback `x`): `bird.setInvisible(false)` -- makes bird visible/active

**`b(bird)` -> `scheduleBirdMarkRemovable(bird)`:**
- One-shot 4.0s timer (callback `y`): `bird.setHasBeenHit(true)` -- marks bird as removable

#### ITEM SPAWNING (inside timer `g` / time counter callback)

Every second tick, if NO potion item and NO shield item on screen, AND player is not invisible/shielded, AND game is not over:
- Random 0-159:
  - 1-8 (5% chance): Spawn POTION at random position -> `m(randomX, randomY)`
  - 9-13 (3.125% chance): Spawn SHIELD at random position -> `n(randomX, randomY)`

**`m(float, float)` -> `spawnPotionItem(x, y)`:**
1. Creates sprite with `potionInvStatRegion` (potioninv-stat.png), size 32x32
2. Places at given position
3. Attaches to scene
4. Sets `potionItemOnScreen = true`
5. Starts potion removal timer `j()` -> 6 seconds to pick up before it disappears

**`n(float, float)` -> `spawnShieldItem(x, y)`:**
1. Creates sprite with `shieldStatRegion` (shield-stat.png), size 32x32
2. Places at given position
3. Attaches to scene
4. Sets `shieldItemOnScreen = true`
5. Starts shield removal timer `k()` -> 6 seconds to pick up

**`l(float, float)` -> `spawn1UpItem(x, y)`:**
1. Creates `OneUpItem` with `lifeStatRegion` (life-stat.png, 4x1 tiled), size 126x32
2. Places at the POSITION WHERE THE RED BIRD DIED
3. Attaches to scene
4. Sets `oneUpItemOnScreen = true`
5. Starts animation frames 0-3
6. Starts 1-UP removal timer `l()` -> 6 seconds to pick up

**1-UP only spawns when a RED bird is killed and no 1-UP is already on screen.**

#### GAME LOOP / UPDATE HANDLER (class `u`)

The `u` class is registered as an update handler. Each frame it calls:
1. `aC()` -> `checkBirdLifeAndRemoval()`
2. `aD()` -> `checkPlayerBirdCollisions()`
3. Item pickup checks (potion, shield, 1-UP)

**`aC()` -> `checkBirdLifeAndRemoval()`:**
If game not stopped, iterates all birds:
- If bird's `remainingBounces < 1` AND bird is not null:
  - Calls `b(bird)` to schedule mark-removable timer (4s)
  - If bird `hasBeenHit()`:
    - Destroys physics body
    - Detaches from scene
    - Disposes resources
    - Sets `markedForRemoval = true`
    - **If bird type is RED and no 1-UP on screen**: spawns 1-UP at bird's death position
- If bird `isMarkedForRemoval()`: removes from bird list, garbage collect

**`aD()` -> `checkPlayerBirdCollisions()`:**
If game not stopped, for each bird:
- Collision check: `bird.collidesWith(player)` AND bird is NOT invisible AND NOT marked for removal AND NOT already hit
- **If player is NOT dead, NOT has potion, NOT has shield (unprotected):**
  1. Increment `birdsKilledCount`
  2. Play PUNCH sound (`resourceManager.punchSound.play()`)
  3. Set bird bounces to 0 (kill the bird)
  4. Set player `hasPotionInvisibility = true` (brief invincibility after hit)
  5. Change player animation to frames 4-7 (hit/damaged sprite)
  6. Decrement player lives by 1
  7. Update lives text: "x {lives}"
  8. Start invincibility timer `n()` -> 4 seconds
  9. If bird was YELLOW: increment `yellowBirdsKilledCount`
  10. If lives <= 0: play PLAYER_DIE sound, set player dead
  11. If player is dead: stop animation, set frame 12, call `as()` (game over)

- **If player HAS SHIELD:**
  1. Play PUNCH sound
  2. Set bird bounces to 0 (kill the bird)
  3. Increment `birdsKilledCount`
  4. If bird was YELLOW: increment `yellowBirdsKilledCount`
  5. **THE SHIELD IS NOT REMOVED ON HIT** - player keeps the shield, bird just dies

**IMPORTANT: When player has POTION (invincibility), collisions are NOT checked at all because the `!hasPotionInvisibility` condition in the first if-block prevents any collision processing for unshielded player, and the shield block only runs `if (hasShield)`. So with potion active, birds pass through the player completely - no collision, no kill.**

#### ITEM PICKUP (also in class `u` update handler)

**Potion pickup:**
- If `potionItemOnScreen` AND `player.collidesWith(potionSprite)`:
  1. Increment `potionsUsedCount`
  2. Play POTION_USE sound (`resourceManager.potionUseSound.play()`)
  3. Set player `hasPotionInvisibility = true`
  4. Change player animation to frames 4-7 (same as hit animation)
  5. Start potion duration timer `i()` -> 13 seconds
  6. Remove potion sprite from scene
  7. Set `potionItemOnScreen = false`

**Shield pickup:**
- If `shieldItemOnScreen` AND `player.collidesWith(shieldSprite)`:
  1. Increment `shieldsUsedCount`
  2. Play SHIELD_USE sound (`resourceManager.shieldUseSound.play()`)
  3. Set player `hasShield = true`
  4. Change player animation to frames 8-11 (shield sprite)
  5. Start shield duration timer `m()` -> 6 seconds
  6. Remove shield sprite from scene
  7. Set `shieldItemOnScreen = false`

**1-UP pickup:**
- If `oneUpItemOnScreen` AND `player.collidesWith(oneUpItem)`:
  1. Play 1UP sound (`resourceManager.oneUpSound.play()`)
  2. Increment player lives: `player.setLives(player.getLives() + 1)`
  3. Remove 1-UP sprite from scene
  4. Update lives text: "x {lives}"
  5. Set `oneUpItemOnScreen = false`

#### TIMER EXPIRY CALLBACKS

**`h` (class) -> potionDurationExpiry (13 seconds):**
- Set player `hasPotionInvisibility = false`
- Change player animation back to frames 0-3 (normal)
- Set `potionItemOnScreen = false` (redundant safety)

**`i` (class) -> potionSpriteRemoval (6 seconds):**
- Remove potion sprite from scene (`.J()` = detach)
- Set `potionItemOnScreen = false`

**`j` (class) -> shieldSpriteRemoval (6 seconds):**
- Remove shield sprite from scene
- Set `shieldItemOnScreen = false`

**`k` (class) -> oneUpSpriteRemoval (6 seconds):**
- Remove 1-UP sprite from scene
- Set `oneUpItemOnScreen = false`

**`l` (class) -> shieldDurationExpiry (6 seconds):**
- Play SHIELD_LOST sound
- Set player `hasShield = false`
- Change player animation back to frames 0-3 (normal)
- Set `shieldItemOnScreen = false` (redundant safety)

**`m` (class) -> invincibilityAfterHitExpiry (4 seconds):**
- Set player `hasPotionInvisibility = false`
- Change player animation back to frames 0-3 (normal)
- Set `shieldItemOnScreen = false` (redundant - should be potionItemOnScreen)

#### GAME OVER SEQUENCE (`as()` method)

1. Show banner ad: `GameActivity.showAd(true)`
2. Stop game music
3. Play game over music
4. Remove HUD elements (time label, time value, life icon, lives text)
5. Set `isGameStopped = true` (via `aG()`)
6. Set ALL cancellation flags to true (stops all timers)
7. Remove camera bounds
8. Stop multitouch
9. Create game over popup (class `d`) with `popup_gameover.png`
10. Position popup centered, offset 30px up
11. Create score text with current kill count
12. Calculate time: `currentTimeMillis = minutes_to_millis(minutes) + seconds_to_millis(seconds)`
13. Get high score from SharedPreferences
14. If current time > high score: save new high score, format as "MM.SS"
15. Else: use stored high score for display
16. Submit achievements (see below)
17. Create time record text below score
18. Show interstitial ad (first time always, then random chance 1/5)
19. Submit leaderboard scores
20. Create game over button menu (Play again + Rank)
21. Vibrate device (100ms)
22. Set `isGameOver = true`
23. Reset counters

**Achievement checks at game over:**
- `achievementFirstBlood`: player died (always true at game over)
- `achievementTooSlow`: survived > 20 seconds
- `achievementAmazing`: survived > 2 minutes (120000ms)
- `achievementVeryFast`: survived > 5 minutes (300000ms)
- `achievement20Minutes`: survived > 20 minutes (1200000ms)
- `achievementKillerJr`: killed >= 10 birds
- `achievementBirdSpanker`: killed >= 15 birds
- `achievementBirdHomicide`: killed >= 30 birds
- `achievement1000Birds`: killed >= 200 birds
- `potionOfInvisibility`/`viciousPotion`: incremental, potionsUsedCount
- `medievalSoldier`/`eliteMedievalSoldier`: incremental, shieldsUsedCount
- `yellowBirdKiller`: incremental, yellowBirdsKilledCount

#### GAME OVER MENU (class `q`)
Button index 0 = PLAY (restart): plays menu-click, stops game-over music, starts game music, loads game scene
Button index 1 = RANK: plays menu-click, shows rank dialog

#### RANK DIALOG (class `r`)
Shows AlertDialog with two buttons:
- Positive: "Rank Time" -> opens time leaderboard
- Negative: "Rank Kills" -> opens kills leaderboard

**`b()` -> `onBackPressed()`:**
- Hides ads
- Reloads menu music resources
- Stops game/gameover music
- Starts menu music
- Removes game over text/HUD
- Returns to menu scene

**`d()` -> `disposeScene()`:**
- Clears HUD
- Removes all timers
- Destroys all bird physics bodies, detaches all birds
- Clears bird list
- Player cleanup

### f/f.java -- GAME BACKGROUND SPRITE
Custom Sprite that overrides drawing to call `fVar.d()` (disables depth test).

### f/g.java -- TIME COUNTER TIMER CALLBACK
See timer `n` description above. Also handles ITEM SPAWNING logic each tick.

### f/h.java -- POTION DURATION EXPIRY (13s)
When timer fires: `player.setPotionInvisibility(false)`, change animation to frames 0-3, `potionItemOnScreen = false`

### f/i.java -- POTION SPRITE REMOVAL (6s)
Removes potion sprite from scene after 6 seconds if not picked up.

### f/j.java -- SHIELD SPRITE REMOVAL (6s)
Removes shield sprite from scene after 6 seconds if not picked up.

### f/k.java -- 1-UP SPRITE REMOVAL (6s)
Removes 1-UP sprite from scene after 6 seconds if not picked up.

### f/l.java -- SHIELD DURATION EXPIRY (6s)
Plays SHIELD_LOST sound, sets `hasShield = false`, changes animation to frames 0-3.

### f/m.java -- INVINCIBILITY AFTER HIT EXPIRY (4s)
Sets `hasPotionInvisibility = false`, changes animation to frames 0-3.

### f/n.java -- PHYSICS CONTACT LISTENER
**Deobfuscated: BirdBounceContactListener**

Only implements `endContact()`:
- Gets both bodies from contact
- Preserves velocity direction but normalizes to +/-10 for both bodies
- This handles bird-to-bird collisions: when two birds collide, they bounce off each other at velocity 10 in whatever direction they were going

### f/o.java -- INTERSTITIAL AD LOADER
Runs on UI thread, loads interstitial ad.

### f/p.java -- INTERSTITIAL AD CALLBACK
When loaded, shows the interstitial.

### f/q.java -- GAME OVER MENU HANDLER
Handles button clicks on game over popup:
- Button 0 (Play): menu-click sound, stop gameover music, start game music, load game scene
- Button 1 (Rank): menu-click sound, show rank dialog

### f/r.java -- RANK DIALOG BUILDER
Builds AlertDialog with time/kills leaderboard buttons.

### f/s.java -- RANK DIALOG "TIME" BUTTON
Opens time leaderboard on Google Play Games.

### f/t.java -- RANK DIALOG "KILLS" BUTTON
Opens kills leaderboard on Google Play Games.

### f/u.java -- GAME UPDATE HANDLER (GAME LOOP)
**THE CORE GAME LOOP** - registered as update handler, runs every frame.

```
a_(float dt):
  if NOT gameOver:
    1. checkBirdLifeAndRemoval()    // aC()
    2. checkPlayerBirdCollisions()  // aD()

  // Item pickup checks (run even during game over transition):
  if potionItemOnScreen AND player.collidesWith(potion):
    pickup potion
  if shieldItemOnScreen AND player.collidesWith(shield):
    pickup shield
  if oneUpItemOnScreen AND player.collidesWith(oneUp):
    pickup 1-UP
```

### f/v.java -- PLAYER BIRD (TOUCH-DRAGGABLE)
**Deobfuscated: PlayerBirdEntity**

Extends `PlayerBird` (e.e). Overrides touch handler:
- On touch/drag: moves player center to touch position
- `b(touchX - width/2, touchY - height/2)` - centers sprite on finger

### f/w.java -- BIRD SPAWN TIMER CALLBACK
Every 5 seconds, spawns a bird at random position (x: 30-770, y: 30-450) if game not stopped.

### f/x.java -- BIRD APPEAR TIMER CALLBACK (3s)
After 3 seconds: `bird.setInvisible(false)` - bird becomes visible and active.

### f/y.java -- BIRD MARK REMOVABLE TIMER CALLBACK (4s)
After 4 seconds: `bird.setHasBeenHit(true)` - bird is marked for removal.

### f/z.java -- LOADING SCENE
**Deobfuscated: LoadingScene**
Simple scene with "Loading..." text in center, green background color.

### f/aa.java -- MENU SCENE
**Deobfuscated: MenuScene**

**Layout:**
- Background image centered at (400, 240)
- Logo sprite at (410, 340)
- Copyright sprite at (400, 15)
- Button menu with 5 buttons:
  - Button 0: Play (position: offset left)
  - Button 1: Rank (position: offset right)
  - Button 2: Rate (position: center)
  - Button 3: Achievements (position: bottom-right area)
  - Button 4: About (position: far bottom-right)

**Button handlers:**
- Play (0): If signed in, stop menu music and load game. If not, try sign-in then load game anyway.
- Rank (1): Show rank dialog (time/kills leaderboard selection)
- Rate (2): Open Play Store page
- Achievements (3): Open Google Play achievements screen
- About (4): Load about scene

**`b()` -> `onBackPressed()`:** Destroys the ad view and calls `System.exit(0)`

### f/ab.java, f/ac.java, f/ad.java -- MENU SPRITE HELPERS
Custom Sprites for menu background, logo, and copyright that override drawing.

### f/ae.java -- MENU RANK DIALOG
Same as game over rank dialog but for the menu.

### f/af.java, f/ag.java -- MENU RANK DIALOG BUTTONS
Time and Kills leaderboard buttons for menu rank dialog.

### f/ah.java -- SPLASH SCENE
**Deobfuscated: SplashScene**
- Shows splash.png sprite at (400, 240) with scale 1.5
- Background color: (0.0, 0.34, 0.22) - dark green

### f/ai.java -- SPLASH SPRITE
Custom Sprite for splash screen that overrides drawing.

### f/a.java -- ABOUT SCENE
**Deobfuscated: AboutScene**
- Background image at (400, 240)
- About image at (450, 300)
- Tapping the about image returns to menu
- Unlocks "Meet the Team" achievement if signed in

### f/b.java -- ABOUT BACKGROUND SPRITE
### f/c.java -- ABOUT BACK BUTTON
Tap handler that goes back to menu.

### f/d.java -- GAME OVER POPUP
Creates sprite with `popup_gameover.png` texture, positioned centered with camera offset.

---

## COMPLETE PLAYER ANIMATION FRAME MAP

The player sprite (birdPlayer.png) is a 4x4 tilesheet = 16 frames:
- **Frames 0-3**: Normal player (facing down/default) - IDLE/NORMAL state
- **Frames 4-7**: Hit/Potion state (invincibility appearance) - after being hit or picking up potion
- **Frames 8-11**: Shield state - when shield is active
- **Frame 12**: Dead frame - single static frame when player dies

## COMPLETE ENEMY BIRD ANIMATION FRAME MAP

Each enemy bird sprite (birdXXX.png) is a 4x4 tilesheet = 16 frames:
- **Frames 0-3**: Default/downward facing animation
- **Frames 4-7**: Facing LEFT animation (moving right-to-left, i.e. when bouncing off right wall)
- **Frames 8-11**: Facing RIGHT animation (moving left-to-right, i.e. when bouncing off left wall)
- **Frame 12**: Death/inactive frame

---

## COMPLETE SOUND EFFECTS TIMELINE

| Event | Sound | File |
|-------|-------|------|
| Game starts | Game music starts | mfx/game-music.ogg |
| Player hit by bird (unshielded) | Punch | sfx/punch.ogg |
| Player dies (lives=0) | Player die | sfx/player-die.ogg |
| Game over screen | Game over music | mfx/gameover-music.ogg |
| Pick up potion | Potion use | sfx/potioninv-use.ogg |
| Pick up shield | Shield use | sfx/shield-use.ogg |
| Shield expires (6s) | Shield lost | sfx/shield-lost.ogg |
| Pick up 1-UP | 1-UP | sfx/1up.ogg |
| Any menu button click | Menu click | sfx/menu-click.ogg |
| Menu screen | Menu music | mfx/menu-music.ogg |
| Game over (device) | Vibrate 100ms | (haptic) |

---

## COMPLETE HUD LAYOUT

During gameplay:
- **Top-left**: "Time:" label at (10, 440) using 8-BIT WONDER font, 30pt
- **Next to time label**: Time value at (150, 440) showing "M.S" format
- **Top-right**: Life icon (life.png) at (690, timeY + 13)
- **Next to life icon**: Lives text at (lifeIcon.x + 20, timeY) showing "x 5"

During game over:
- HUD is removed
- Game over popup centered
- Score text centered on popup (kill count)
- Time record text below score
- Play/Rank buttons on popup

---

## COMPLETE COLLISION BEHAVIOR SUMMARY

### Player vs Bird (NO protection):
1. Bird bounces = 0 (will be removed)
2. Player becomes invincible (hasPotionInvisibility = true) for 4 seconds
3. Player sprite changes to frames 4-7 (hit appearance)
4. Player loses 1 life
5. Punch sound plays
6. If bird was yellow, yellow kill counter increments
7. If lives = 0: player die sound, player dead, game over

### Player vs Bird (SHIELD active):
1. Bird bounces = 0 (will be removed)
2. Birds killed counter increments
3. Punch sound plays
4. **Shield remains active** - it is NOT consumed by the hit
5. If bird was yellow, yellow kill counter increments
6. Player does NOT lose a life
7. Player sprite stays in frames 8-11 (shield)

### Player vs Bird (POTION/INVINCIBILITY active):
1. **NO collision detected at all** - the collision check skips because `hasPotionInvisibility` is true
2. Birds fly through the player
3. No sound, no damage, no bird killed

### Bird reaching bounce count 0 (natural death):
1. Animation stops, frame set to 12 (death frame)
2. Bird becomes invisible (alpha 0.5)
3. Physics body deactivated
4. After 4 seconds, bird is fully removed
5. If RED bird: spawns 1-UP at death position (if no 1-UP already on screen)

### Bird-to-Bird collision (physics):
1. Both birds bounce off each other
2. Velocity normalized to +/-10 in whatever direction they were going
3. No damage to either bird, no bounce counter change

---

## COMPLETE ITEM SYSTEM

### Shield Item
- **Spawn**: Random chance every second (3.125% per tick, indices 9-13 out of 0-159)
- **Condition**: Only spawns if no potion AND no shield item currently on screen, AND player has no active potion/shield, AND game not over
- **Appearance**: shield-stat.png, 32x32 sprite
- **Position**: Random (x: 30-770, y: 30-450)
- **Pickup**: Player touches/overlaps the shield sprite
- **Effect on pickup**:
  - Shield use sound plays
  - Player hasShield = true
  - Player sprite -> frames 8-11 (shield appearance)
  - Shield pickup counter increments
- **Duration**: 6 seconds after pickup
- **Expiry**: Shield lost sound, player hasShield = false, sprite -> frames 0-3
- **Despawn**: If not picked up within 6 seconds, removed from screen
- **Behavior when hit**: Bird dies, shield stays, no damage to player

### Potion Item
- **Spawn**: Random chance every second (5% per tick, indices 1-8 out of 0-159)
- **Condition**: Same as shield - only if no items on screen, player not protected, not game over
- **Appearance**: potioninv-stat.png, 32x32 sprite
- **Position**: Random (x: 30-770, y: 30-450)
- **Pickup**: Player touches/overlaps the potion sprite
- **Effect on pickup**:
  - Potion use sound plays
  - Player hasPotionInvisibility = true
  - Player sprite -> frames 4-7 (invincibility appearance, same as hit)
  - Potion use counter increments
- **Duration**: 13 seconds after pickup
- **Expiry**: Player hasPotionInvisibility = false, sprite -> frames 0-3
- **Despawn**: If not picked up within 6 seconds, removed from screen
- **Behavior**: ALL collisions ignored, birds pass through

### 1-UP Item
- **Spawn**: ONLY when a RED bird dies (bounces reach 0) AND no 1-UP already on screen
- **Appearance**: life-stat.png, 126x32 animated sprite (4x1 tiles, frames 0-3)
- **Position**: At the exact location where the red bird died
- **Pickup**: Player touches/overlaps the 1-UP sprite
- **Effect on pickup**:
  - 1-UP sound plays
  - Player lives += 1
  - Lives text updated
- **Duration**: N/A (instant effect)
- **Despawn**: If not picked up within 6 seconds, removed from screen

---

## PHYSICS DETAILS

- Gravity: Vector2(0, 0) -- zero gravity (birds float)
- Contact listener bounces birds off each other at velocity 10
- Bird fixture: density=0, elasticity=1.0 (perfect bounce), friction=0
- Birds start with velocity (-speed, 10) -- moving left and slightly down
- Wall bouncing handled manually in bird's onManagedUpdate, not by physics walls
- Physics world only handles bird-to-bird collisions

---

## HIGH SCORE SYSTEM

- SharedPreferences name: "trash"
- Key: "tyiq" (long, milliseconds)
- Calculated as: `TimeUnit.MINUTES.toMillis(minutes) + TimeUnit.SECONDS.toMillis(seconds)`
- Only saves if current time > stored time
- Displayed as "MM.SS" format on game over

---

## INTERSTITIAL AD LOGIC

- Ad unit: `ca-app-pub-5890786745182778/5224636848`
- First game over: ALWAYS shows interstitial
- Subsequent game overs: Random chance 1 in 5 (`Random.nextInt(5) == 1`)
- The `b.a.b` flag tracks whether first game over has occurred
- `b.a.c = 5` is the random frequency

---

## GOOGLE PLAY LOGIN

- SharedPreferences name: "settings"
- Key: "glogin" (boolean)
- On first play, if not logged in, tries to sign in
- Saves login state for future sessions
