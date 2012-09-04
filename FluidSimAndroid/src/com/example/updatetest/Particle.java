package com.example.updatetest;

import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.sprite.Sprite;

public class Particle {
	
	//For Springs
	static double plasticity=.3;
	
	static double restDensity=1600;
	static double stiffness=.004;
	static double nearStiffness=.01; 
	
	//For Viscosity
	static float theta = .1f;
	static float beta = 0f;
	
	static int serial=0;
	
	float x;
	float y;
	float xPrev;
	float yPrev;
	float density;
	float nearDensity;
	float pressure;
	float nearPressure;
	float[] velocity =new float[2];

	
	Sprite circle = null;
	
	int idNum;
	
	public String toString(){
		return "Particle Id="+idNum;
	}
	Particle(float x, float y){
		this.x=x;
		this.y=y;
		velocity[0]=0;
		velocity[1]=0;
		circle = new Sprite(x,y, MainActivity.mTextureRegion, MainActivity.getSharedInstance().getVertexBufferObjectManager());
		idNum=serial;
		serial++;
	}
	void setVelocity(float newX, float newY){
		velocity[0]=newX;
		velocity[1]=newY;
	}
	void recalculateVelocity(){
		setVelocity(x-xPrev,y-yPrev);
	}
	void setPreviousPosition(float x, float y){
		xPrev=x;
		yPrev=y;
	}
	void setPredictedPostion(float[] velocity){
		this.x+=velocity[0];
		this.y+=velocity[1];
	}
	void compensateForContainerCollisions(){
		if(x>MainActivity.safeRight){
			x=MainActivity.safeRight;
		}
		else if (x<MainActivity.safeLeft){
			x=MainActivity.safeLeft;
		}
		if (y>MainActivity.safeBottom){
			y=MainActivity.safeBottom;
		}
		else if (y<MainActivity.safeTop){
			y=MainActivity.safeTop;
		}
	}
	void updateCircle(){
		float newX=x-(circle.getWidth()/2);
		float newY=y-(circle.getHeight()/2);
		circle.setPosition(newX, newY);
	}

}

