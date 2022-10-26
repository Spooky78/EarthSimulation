package com.example.earthsimulation;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.*;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

public class EarthSimulation extends Application {
    private static final String EARTH_DAY_MAP_PATH = "earth_daymap.jpg";
    private static final String EARTH_NIGHT_MAP_PATH = "earth_nightmap.jpg";
    private static final String EARTH_NORMAL_MAP_PATH = "earth_normal_map.jpg";
    private static final String EARTH_SPECULAR_PATH = "earth_specular.jpg";
    private static final String MOON_MAP = "moon.jpg";
    private static final String GALAXY_BACKGROUND = "galaxy_background.jpg";
    private static final int WIDTH = 1400;
    private static final int HEIGHT = 1000;
    private double anchorX, anchorY;
    private double anchorAngleX = 0;
    private double anchorAngleY = 0;
    private final DoubleProperty angleX = new SimpleDoubleProperty(0);
    private final DoubleProperty angleY = new SimpleDoubleProperty(0);
    private final Sphere earth = new Sphere(200);
    private final PointLight moonLight = new PointLight();

    @Override
    public void start(Stage primaryStage) {
        Group root = new Group();
        root.getChildren().add(prepareImageView());

        SmartGroup galaxy = new SmartGroup();
        root.getChildren().add(galaxy);
        galaxy.getChildren().add(createEarth());
        galaxy.getChildren().addAll(createMoon());

        Camera camera = new PerspectiveCamera();

        galaxy.translateXProperty().set(WIDTH / 2.0);
        galaxy.translateYProperty().set(HEIGHT / 2.0);

        Slider slider = createSlider();
        galaxy.translateZProperty().bind(slider.valueProperty());
        root.getChildren().add(slider);

        Scene scene = new Scene(root, WIDTH, HEIGHT, true);
        //scene.setFill(Color.BLACK);
        scene.setCamera(camera);

        initMouseControl(galaxy, scene, primaryStage);

        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()) {
                case MINUS -> galaxy.translateZProperty().set(galaxy.getTranslateZ() + 50);
                case EQUALS -> galaxy.translateZProperty().set(galaxy.getTranslateZ() - 50);
                case UP -> galaxy.rotateByX(10);
                case DOWN -> galaxy.rotateByX(-10);
                case LEFT -> galaxy.rotateByY(10);
                case RIGHT -> galaxy.rotateByY(-10);
            }
        });

        primaryStage.setTitle("Earth Simulation!");
        primaryStage.setScene(scene);
        primaryStage.show();

        createAnimation();
    }

    private void createAnimation(){
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                earth.rotateProperty().set(earth.getRotate() + 0.02);
                moonLight.setRotate(moonLight.getRotate() + 0.1);
            }
        };
        timer.start();
    }

    private ImageView prepareImageView(){
        Image image = new Image(GALAXY_BACKGROUND);
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.getTransforms().add(new Translate(-image.getWidth()/2,-image.getHeight()/2, 5000));
        return imageView;
    }

    private Node createEarth(){
        PhongMaterial earthMaterial = new PhongMaterial();
        earthMaterial.setDiffuseMap(new Image(EARTH_DAY_MAP_PATH));
        earthMaterial.setSelfIlluminationMap(new Image(EARTH_NIGHT_MAP_PATH));
        earthMaterial.setSpecularMap(new Image(EARTH_SPECULAR_PATH));
        earthMaterial.setBumpMap(new Image(EARTH_NORMAL_MAP_PATH));

        earth.setRotationAxis(Rotate.Y_AXIS);
        earth.setMaterial(earthMaterial);
        earth.setTranslateY(-50);
        return earth;
    }

    private Node[] createMoon(){
        moonLight.setColor(Color.WHITE);
        moonLight.getTransforms().add(new Translate(0,-50,-500));
        moonLight.setRotationAxis(Rotate.Y_AXIS);

        PhongMaterial moonMaterial = new PhongMaterial();
        moonMaterial.setDiffuseMap(new Image(MOON_MAP));
        moonMaterial.setSelfIlluminationMap(new Image(MOON_MAP));

        Sphere moon = new Sphere(30);
        moon.setMaterial(moonMaterial);
        moon.getTransforms().setAll(moonLight.getTransforms());
        moon.rotateProperty().bind(moonLight.rotateProperty());
        moon.rotationAxisProperty().bind(moonLight.rotationAxisProperty());
        return new Node[] {moonLight, moon};
    }

    private Slider createSlider(){
        Slider slider = new Slider();
        slider.setMax(1000);
        slider.setMin(-1000);
        slider.setPrefWidth(1000d);
        slider.setLayoutX(WIDTH/2.0 - 500);
        slider.setLayoutY(900);
        slider.setShowTickLabels(true);
        slider.setTranslateZ(5);
        slider.setStyle("-fx-base: black");
        return slider;
    }

    private void initMouseControl(SmartGroup group, Scene scene, Stage stage){
        Rotate xRotate;
        Rotate yRotate;
        group.getTransforms().addAll(
                xRotate = new Rotate(0, Rotate.X_AXIS),
                yRotate = new Rotate(0, Rotate.Y_AXIS)
        );
        xRotate.angleProperty().bind(angleX);
        yRotate.angleProperty().bind(angleY);

        scene.setOnMousePressed(event -> {
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
            anchorAngleX = angleX.get();
            anchorAngleY = angleY.get();
        });

        scene.setOnMouseDragged(event -> {
            angleX.set(anchorAngleX - (anchorY - event.getSceneY()));
            angleY.set(anchorAngleY + anchorX - event.getSceneX());
        });

        stage.addEventHandler(ScrollEvent.SCROLL, event -> {
            double delta = event.getDeltaY();
            group.translateZProperty().set(group.getTranslateZ() + delta);
        });
    }

    public static void main(String[] args) {
        launch();
    }
    static class SmartGroup extends Group {

        Rotate r;
        Transform t = new Rotate();

        void rotateByX(int ang) {
            r = new Rotate(ang, Rotate.X_AXIS);
            t = t.createConcatenation(r);
            this.getTransforms().clear();
            this.getTransforms().addAll(t);
        }

        void rotateByY(int ang) {
            r = new Rotate(ang, Rotate.Y_AXIS);
            t = t.createConcatenation(r);
            this.getTransforms().clear();
            this.getTransforms().addAll(t);
        }
    }
}