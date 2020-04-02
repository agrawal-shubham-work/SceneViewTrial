package com.example.sceneviewtrial;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.SceneView;
import com.google.ar.sceneform.TouchEventSystem;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.FootprintSelectionVisualizer;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.ar.sceneform.ux.TransformationSystem;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private SceneView sceneView;
    private Scene scene;

    private TransformationSystem transformationSystem;
    private ModelRenderable renderable,pinRenderable;
    private boolean modelPlaced=false;
    private TransformableNode modelNode;
    private Node mainNode;
    private ArrayList<Vector3> postionArray;
    private ArrayList<Quaternion> rotationArray;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createScene();
        createPinScene();
        setContentView(R.layout.activity_main);

        setArrayValue();



        sceneView = findViewById(R.id.sceneView);
        scene = sceneView.getScene();
        transformationSystem=new TransformationSystem(getResources().getDisplayMetrics(),new FootprintSelectionVisualizer());
        createScene();

        scene.addOnUpdateListener(this::onFrameUpdate);
        sceneView.getRenderer().setClearColor(new com.google.ar.sceneform.rendering.Color(Color.LTGRAY));
        scene.addOnPeekTouchListener(new Scene.OnPeekTouchListener() {
            @Override
            public void onPeekTouch(HitTestResult hitTestResult, MotionEvent motionEvent) {
                transformationSystem.onTouch(hitTestResult,motionEvent);

            }
        });

    }


    private void createPinScene() {
        ModelRenderable
                .builder()
                .setSource(this,Uri.parse("Pin.sfb"))
                .build()
                .thenAccept(renderable ->{
                    pinRenderable=renderable;
                })
                .exceptionally(throwable -> {
                    Toast.makeText(this, "Inable to render pin Model", Toast.LENGTH_SHORT).show();
                   return null;
                });
    }

    private void createScene() {
        ModelRenderable.builder()
                .setSource(this, Uri.parse("Violin.sfb"))
                .build()
                .thenAccept(renderable -> {
                    this.renderable=renderable;
                })
                .exceptionally(throwable -> {
                    Toast.makeText(this, "Unable to render Main Model", Toast.LENGTH_SHORT).show();
                    return null;
                });
    }

    private void onRenderableLoaded() {

        if (renderable==null)
            return;

        /*TransformableNode modelNode=new TransformableNode(transformationSystem);
        modelNode.getRotationController().setEnabled(true);
        modelNode.getScaleController().setEnabled(true);
        modelNode.getTranslationController().setEnabled(false);
        modelNode.setRenderable(renderable);
        scene.addChild(modelNode);
        modelNode.setLocalPosition(new Vector3(0, 0, -1.5f));
        transformationSystem.selectNode(modelNode);*/

        modelNode=new TransformableNode(transformationSystem);
        modelNode.getRotationController().setEnabled(true);
        modelNode.getScaleController().setEnabled(true);
        modelNode.getTranslationController().setEnabled(false);
        modelNode.getScaleController().setMinScale(0.5f);
        modelNode.getScaleController().setMaxScale(1.4f);
        modelNode.setLocalPosition(new Vector3(1f,-1f,-6f));
        modelNode.setParent(scene);


        mainNode=new Node();
        mainNode.setRenderable(renderable);
        mainNode.setParent(modelNode);

        transformationSystem.selectNode(modelNode);
        Log.d("Placed","Placed renderable");
        modelPlaced=true;

    }


    private void setArrayValue() {
        postionArray=new ArrayList<>();
        rotationArray=new ArrayList<>();

        postionArray.add(new Vector3(0f,0.5f,3.8f));
        postionArray.add(new Vector3(1.5f,1.3f,-0.5f));
        postionArray.add(new Vector3(-1.9f,0.5f,1f));

        rotationArray.add(new Quaternion(new Vector3(0f,1.5f,1f),90));
        rotationArray.add(new Quaternion(new Vector3(0f,0f,-0.6f),90));
        rotationArray.add(new Quaternion(new Vector3(0.5f,0f,0.3f),90));

    }

    private void onFrameUpdate(FrameTime frameTime) {
        if(!modelPlaced){
            onRenderableLoaded();
            for (int i=0;i<3;i++)
            {
                Node pinNode=new Node();
                pinNode.setRenderable(pinRenderable);
                pinNode.setName(""+i);
                pinNode.setLocalScale(new Vector3(0.2f,0.2f,0.2f));
                pinNode.setLocalPosition(postionArray.get(i));
                pinNode.setLocalRotation(rotationArray.get(i));
                pinNode.setParent(mainNode);

                pinNode.setOnTouchListener(new Node.OnTouchListener() {
                    @Override
                    public boolean onTouch(HitTestResult hitTestResult, MotionEvent motionEvent) {
                        callTapMethod(pinNode.getName());
                        return true;
                    }
                });
            }
        }



    }

    private void callTapMethod(String name) {
        Toast.makeText(this, name, Toast.LENGTH_SHORT).show();
    }

    private void onRenderedPinLoaded(Vector3 position,Quaternion rotation,String name) {
        if (pinRenderable==null)
            return;



    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            sceneView.resume();
        }
        catch (CameraNotAvailableException e){}
    }

    @Override
    protected void onPause() {
        super.onPause();
        sceneView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sceneView.destroy();
    }
}

