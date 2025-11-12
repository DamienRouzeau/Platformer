package com.mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.Spatial;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;

public class Main extends SimpleApplication implements ActionListener {
    
    private BulletAppState bulletAppState;
    private BetterCharacterControl playerControl;
    //private Geometry playerGeom;
    private Spatial playerModel;
    
    private boolean left = false, right = false, forward = false, backward = false;
    private Vector3f walkDirection = new Vector3f();
    private Vector3f camDir = new Vector3f();
    private Vector3f camLeft = new Vector3f();
    
    private float timer = 0;

    private BitmapFont font;
    private BitmapText text;
    
    private Geometry finish;
    private Boolean haveWin = false;

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Initialiser la physique
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        
        // Configurer la caméra
        flyCam.setEnabled(false); // Désactiver flyCam par défaut
        cam.setLocation(new Vector3f(0, 5, 10));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        
        // Créer le joueur (sphère pour l'instant)
        createPlayer();
        
        // Créer le sol de test
        createFloor();
        
        // Créer quelques plateformes de test
        createTestPlatforms();
        
        // Créer la zone d'arrivée
        createFinishPlatform();
        
        // Configurer l'éclairage
        setupLighting();
        
        // Configurer les contrôles
        setupKeys();
        
        //Create Timer
        createTimer();
    }
    
    private void createTimer()
    {
        text = new BitmapText(guiFont);
        text.setSize(24);
        text.setLocalTranslation(10, settings.getHeight() - 10, 0);

        // Affichage
        guiNode.attachChild(text);
    }
    
    private void createPlayer() {
        playerModel = assetManager.loadModel("Models/Player/Player.glb");
        /*Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setTexture("DiffuseMap", assetManager.loadTexture("Models/Player/texture.png"));
        playerModel.setMaterial(mat);*/
        
        // Character Control (physique)
        playerControl = new BetterCharacterControl(0.5f, 1.8f, 30f);
        playerModel.addControl(playerControl);
        
        // Position initiale
        playerControl.warp(new Vector3f(0, 5, 0));
        
        // Ajouter à la scène et à la physique
        rootNode.attachChild(playerModel);
        bulletAppState.getPhysicsSpace().add(playerControl);
    }
    
    private String timerFormat()
    {
        int min = (int)timer/60;
        int sec = (int)timer - (min*60);
        String format = String.format("%02d:%02d", min, sec);
        return format;
    }
    
    private void createFloor() {
        Box floor = new Box(20, 0.1f, 20);
        Geometry floorGeom = new Geometry("Floor", floor);
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setColor("Diffuse", ColorRGBA.Gray);
        mat.setColor("Ambient", ColorRGBA.DarkGray);
        mat.setBoolean("UseMaterialColors", true);
        floorGeom.setMaterial(mat);
        floorGeom.setLocalTranslation(0, -0.1f, 0);
        
        // Ajouter collision physique
        floorGeom.addControl(new com.jme3.bullet.control.RigidBodyControl(0));
        rootNode.attachChild(floorGeom);
        bulletAppState.getPhysicsSpace().add(floorGeom);
    }
    
    private void createTestPlatforms() {
        createPlatform(new Vector3f(5, 1, 0), ColorRGBA.Green);
        createPlatform(new Vector3f(10, 2, 0), ColorRGBA.Green);
        createPlatform(new Vector3f(15, 3, 0), ColorRGBA.Green);
    }
    
    private void createFinishPlatform()
    {
        finish = createPlatform(new Vector3f(20, 4, -5), ColorRGBA.Magenta);
    }
    
    private Geometry createPlatform(Vector3f position, ColorRGBA color) {
        Box box = new Box(2, 0.2f, 2);
        Geometry geom = new Geometry("Platform", box);
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setColor("Diffuse", color);
        mat.setColor("Ambient", color.mult(0.5f));
        mat.setBoolean("UseMaterialColors", true);
        geom.setMaterial(mat);
        geom.setLocalTranslation(position);
        
        // Ajouter collision
        geom.addControl(new com.jme3.bullet.control.RigidBodyControl(0));
        rootNode.attachChild(geom);
        bulletAppState.getPhysicsSpace().add(geom);
        return geom;
    }
    
    private void setupLighting() {
        // Lumière directionnelle (soleil)
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.5f, -1, -0.5f).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);
        
        // Lumière ambiante
        com.jme3.light.AmbientLight ambient = new com.jme3.light.AmbientLight();
        ambient.setColor(ColorRGBA.White.mult(0.3f));
        rootNode.addLight(ambient);
        
        // Ciel bleu
        viewPort.setBackgroundColor(new ColorRGBA(0.5f, 0.7f, 1.0f, 1.0f));
    }
    
    private void setupKeys() {
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A), 
                                       new KeyTrigger(KeyInput.KEY_Q)); // AZERTY
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Forward", new KeyTrigger(KeyInput.KEY_W), 
                                          new KeyTrigger(KeyInput.KEY_Z)); // AZERTY
        inputManager.addMapping("Backward", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        
        inputManager.addListener(this, "Left", "Right", "Forward", "Backward", "Jump");
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if(haveWin)return;
        if (name.equals("Left")) {
            left = isPressed;
        } else if (name.equals("Right")) {
            right = isPressed;
        } else if (name.equals("Forward")) {
            forward = isPressed;
        } else if (name.equals("Backward")) {
            backward = isPressed;
        } else if (name.equals("Jump") && isPressed) {
            playerControl.jump();
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        // Calculer la direction de marche basée sur la caméra
        camDir.set(cam.getDirection()).setY(0).normalizeLocal();
        camLeft.set(cam.getLeft()).setY(0).normalizeLocal();
        
        walkDirection.set(0, 0, 0);
        if (left) {
            walkDirection.addLocal(camLeft);
        }
        if (right) {
            walkDirection.addLocal(camLeft.negate());
        }
        if (forward) {
            walkDirection.addLocal(camDir);
        }
        if (backward) {
            walkDirection.addLocal(camDir.negate());
        }
        
        
        // Appliquer le mouvement
        playerControl.setWalkDirection(walkDirection.normalizeLocal().multLocal(5f));
        playerControl.setViewDirection(camDir);
        
        // Caméra suit le joueur (troisième personne)
        Vector3f playerPos = playerModel.getWorldTranslation().clone();
        cam.setLocation(playerPos.add(0, 3, 8)); // Caméra derrière et au-dessus
        cam.lookAt(playerPos.add(0, 1, 0), Vector3f.UNIT_Y);
        
        
        // Verification de victoire
        float winDist = playerModel.getWorldTranslation().distance(finish.getLocalTranslation());
        if(winDist < 2f)
        {
            BitmapText finishText = new BitmapText(guiFont);
            finishText.setSize(48);
            finishText.setText("Congratulation! Time : " + timerFormat());
            finishText.setLocalTranslation(settings.getWidth()/2 - 250, settings.getHeight()/2, 0);
            // Affichage
            guiNode.attachChild(finishText);
            haveWin = true;
        }
        else 
        {
        // Gestion du timer
        timer += tpf;
        text.setText(timerFormat());
        }
    }
}