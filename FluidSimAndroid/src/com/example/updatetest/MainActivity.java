package com.example.updatetest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.util.FPSLogger;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.ui.activity.SimpleBaseGameActivity;

import android.content.Context;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

//fuck!!!!!!!!!!!!!!!!!!!!!!!!!!!!


public class MainActivity extends SimpleBaseGameActivity implements SensorEventListener {
	static final int CAMERA_WIDTH = 480;
	static final int CAMERA_HEIGHT = 800;
	static BitmapTextureAtlas mBitmapTextureAtlas;
	static TextureRegion mTextureRegion;
	static MainActivity instance;
	Scene scene;
	

	float gravitySenseX=0;
	float gravitySenseY=.08f;
	
	private SensorManager mSensorManager;
	private Sensor gravitySensor;
	
//Simulation Properties=============================================================================================
//==================================================================================================================
	
	//initialization parameters
	static int startCount=30;
	static int[] startSize = {CAMERA_WIDTH,CAMERA_HEIGHT};
	
	//Particle Parameters
	static float interactionRadius = 30;
	
	//Rendering Parameters
	static float particleCircleRadius = 7;
	
	//Border Parameters
	static float xEdges = startSize[0]-2*particleCircleRadius;
	static float safeRight=startSize[0]-particleCircleRadius;
	static float safeLeft=particleCircleRadius;
	static float safeBottom=startSize[1]-particleCircleRadius;
	static float safeTop=particleCircleRadius;
	
	List <Particle> p = new ArrayList<Particle>(startCount+20); 
	static float[] gravity= {0,.08f};
	
	HashMap<List<Integer>, HashCell> map;	
	HashMap <List<Integer>, List<Particle>> particleNeighborPair = new HashMap <List<Integer>, List<Particle>>();
//_____________________________________________________________________________________________________________________	
	
//AndEngine Convenience Methods========================================================================================
//=====================================================================================================================
	
	//kind of wonkey
	static MainActivity getSharedInstance(){
		return instance;
	}
//______________________________________________________________________________________________________________________	
	

	@Override
	public EngineOptions onCreateEngineOptions() {
		instance = this;
		Camera mCamera = new Camera(0,0,CAMERA_WIDTH,CAMERA_HEIGHT);
		return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), mCamera);
	}

	@Override
	protected void onCreateResources() {
		mBitmapTextureAtlas = new BitmapTextureAtlas(mEngine.getTextureManager(), 16, 16, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		//BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("res\\gfx\\");
		mTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "circle.png", 0, 0);
		mEngine.getTextureManager().loadTexture(mBitmapTextureAtlas);
		
	}
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}
	@Override
	public void onSensorChanged(SensorEvent event) {
		synchronized (this){
			gravitySenseX=-event.values[0]/80;
			gravitySenseY=event.values[1]/80;
		}
		
	}
	@Override
	protected Scene onCreateScene() {
		mEngine.registerUpdateHandler(new FPSLogger(60));
		scene = new Scene();
		scene.setBackground(new Background(0.95f, 0.95f, 0.95f));
		
		
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gravitySensor= mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
		mSensorManager.registerListener(this, gravitySensor,
				SensorManager.SENSOR_DELAY_NORMAL);
		
		setParticleLayout();
		
		scene.registerUpdateHandler(new IUpdateHandler(){
			@Override
			public void onUpdate(float pSecondsElapsed) {
//Simulation Logic Loop==========================================================================
//{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{
				
				//apply gravity
				synchronized (this){
					setGravity(gravitySenseX, gravitySenseY);
				}
		    	for (int j =0; j<p.size();j++){                                                                                                               
		    		Particle particle= p.get(j);        	                                                                                                        
		    		particle.setVelocity(particle.velocity[0]+gravity[0], particle.velocity[1]+gravity[1]);                                                               
		    	}
		    	
				applyViscosityImpulses();     
				
				//predict positions
		    	for (int k=0; k<p.size();k++){                                                                                                              
		    		Particle particle=p.get(k);									                                                                                                                 
		    		//save previous position                                                                                                                
		    		particle.setPreviousPosition(particle.x, particle.y);               
		    		
		    		
		                                                                                                                                                    
		    		//advance without collision/conditional checks. A.k.a predicted position                                                                
		    		moveAndUpdateHash(particle, particle.velocity[0], particle.velocity[1]);
		    	}                                                                                                                                           
		    	                                                                                                                                            
		    	doubleDensityRelaxation();                                                                                                                  
		    	                                                                                                                                            
		    	//resolve solid body collisions                                                                                                             
		    	                                                                                                                                            
		    	for (int i=0;i<p.size();i++){                                                                                                               
		    		//use previous positions to compute velocity
		    		Particle particle = p.get(i);
		    		particle.recalculateVelocity();                                                                                                             
		    		particle.updateCircle();                                                                                                                    
		    	} 
				
//}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}				
			}
			@Override
			public void reset() {
			}	
		});
		return scene;
	}

	
//Primary Algorithm Components===========================================================================================
//=======================================================================================================================
	void applyViscosityImpulses(){
    	particleNeighborPair.clear();
    	
    	//create ParticleNeighborPairs
    	for (int i = 0;i<p.size();i++){    
    		Particle particle= p.get(i);      
    		ArrayList<Particle> Neighbors = getNeighbors(particle);
    		for (int k=0;k<Neighbors.size();k++){
    			List <Integer> pairKey=findPairKey(particle, Neighbors.get(k));
    			if(!particleNeighborPair.containsKey(pairKey)){
    				List<Particle> normalized=createNormalizedParticleList(particle, Neighbors.get(k));
    				particleNeighborPair.put(pairKey, normalized);
    			}
    		}
    	}
    	
    	for (List<Particle> particles:particleNeighborPair.values()){
    		Particle first = particles.get(0);
    		Particle second = particles.get(1);
    		float q =(float) (findDistance(first, second)/interactionRadius);
    		if (q<1){
    			//find inward radial velocity
    			float[] unitVector = findDisplacementUnitVector(first, second);
    			float radialVelocityX = (first.velocity[0]-second.velocity[0])*unitVector[0];
    			float radialVelocityY = (first.velocity[1]-second.velocity[1])*unitVector[1];
    			//apply impulses
    			if (radialVelocityX>0){
    				float impulse = (float) (((1-q)*(Particle.theta*radialVelocityX+Particle.beta*Math.pow(radialVelocityX, 2))*unitVector[0])/2);
    				first.velocity[0]-=impulse;
    				second.velocity[0]+=impulse;
    			}
    			if (radialVelocityY>0){
    				float impulse = (float) (((1-q)*(Particle.theta*radialVelocityY+Particle.beta*Math.pow(radialVelocityY, 2))*unitVector[1])/2);
    				first.velocity[1]-=impulse;
    				second.velocity[1]+=impulse;
    			}
    		}
    	}
    }

	
	
    void doubleDensityRelaxation(){
    	for(int i =0;i<p.size();i++){ 
    		Particle particle = p.get(i); 
    		particle.density=0;
    		particle.nearDensity=0;
    		ArrayList<Particle> Neighbors=getNeighbors(particle);
    		float[] radii=new float[Neighbors.size()];
    		float[] cues=new float[Neighbors.size()];
    		//set the "q"s for each particle
    		for (int k =0;k<Neighbors.size();k++){
    			radii[k]=findDistance(particle, Neighbors.get(k));
    			cues[k]=(float) (radii[k]/interactionRadius);
    		}
    		//compute density and nearDensity
    		for (int k=0;k<Neighbors.size();k++){
    			float q=cues[k];
    			if (q<1){
    				particle.density+=Math.pow(1-q,2);
    				particle.nearDensity+=Math.pow(1-q, 3);
    			}
    		}
    		//compute pressure and nearPressure
    		particle.pressure=(float) (Particle.stiffness*(particle.density-Particle.restDensity));
    		particle.nearPressure=(float) (Particle.nearStiffness*(particle.nearDensity));
    		float dxX=0;
    		float dxY=0;
    		for(int k = 0; k<Neighbors.size();k++){
    			float q=cues[k];
    			if(q<1){
    				//apply displacement
    				Particle neighbor=Neighbors.get(k);
    				
    				float[] unitVector=findDisplacementUnitVector(particle, neighbor);
    				float unitVectorX=unitVector[0];
    				float unitVectorY=unitVector[1];
    				float displacementX=(float) ((particle.pressure*(1-q)+particle.nearPressure*Math.pow((1-q), 2))*unitVectorX);
    				float displacementY=(float) ((particle.pressure*(1-q)+particle.nearPressure*Math.pow((1-q), 2))*unitVectorY);                   
    				moveAndUpdateHash(neighbor, displacementX/2, displacementY/2);                                                                  
    				dxX-=(displacementX/2);                                                                                                         
    				dxY-=(displacementY/2);                                                                                                         
    			}                                                                                                                                   
    		}                                                                                                                                       
    		moveAndUpdateHash(particle,dxX,dxY);                                                                                                    
    	}                                                                                                                                           
    }
//_______________________________________________________________________________________________________________________
	
//Convenience Methods====================================================================================================
//=======================================================================================================================
    
    void setGravity(float x, float y){
    	gravity=new float[] {x, y};
    }
	
    ArrayList<Particle> getNeighbors(Particle particle){
    	
    	List<Integer> centralKey=findCentralHashKey(particle);                                                                                      
    	HashCell centralHashCell=map.get(centralKey);                                                                                               
    	ArrayList<Particle> neighbors=new ArrayList<Particle>();                                                                                    
    	neighbors.addAll(centralHashCell.contained);                                                                                                
    	neighbors.remove(particle);                                                                                                                 
    	int keyX=centralKey.get(0);                                                                                                                 
    	int keyY=centralKey.get(1);                                                                                                                 
    	ArrayList<List<Integer>> otherKeys = new ArrayList<List<Integer>>(8);                                                                       
    	                                                                                                                                            
    	otherKeys.add(Arrays.asList(new Integer[] {keyX-1, keyY-1}));                                                                               
    	otherKeys.add(Arrays.asList(new Integer[] {keyX, keyY-1}));                                                                                 
    	otherKeys.add(Arrays.asList(new Integer[] {keyX+1, keyY-1}));                                                                               
    	                                                                                                                                            
    	otherKeys.add(Arrays.asList(new Integer[] {keyX-1, keyY}));                                                                                 
    	otherKeys.add(Arrays.asList(new Integer[] {keyX+1, keyY}));                                                                                 
    	                                                                                                                                            
    	otherKeys.add(Arrays.asList(new Integer[] {keyX-1, keyY+1}));                                                                               
    	otherKeys.add(Arrays.asList(new Integer[] {keyX, keyY+1}));                                                                                 
    	otherKeys.add(Arrays.asList(new Integer[] {keyX+1, keyY+1}));                                                                               
    	for (List<Integer> key :otherKeys){                                                                                                         
    		if (map.containsKey(key)){                                                                                                              
    			neighbors.addAll(map.get(key).contained);                                                                                           
    		}                                                                                                                                       
    	}                                                                                                                                           
    	return neighbors;                                                                                                                           
    }
    
    ArrayList<HashCell> getCloseHashCells(float[] point){
    	
    	List<Integer> centralKey=findCentralHashKey(point);                                                                                      
    	ArrayList<HashCell> closeCells = new ArrayList<HashCell>(9);
    	closeCells.add(map.get(centralKey));
    	
    	Integer keyX = centralKey.get(0);
    	Integer keyY = centralKey.get(1);
    	
    	closeCells.add(map.get(Arrays.asList(new Integer[] {keyX-1, keyY-1})));                                                                               
    	closeCells.add(map.get(Arrays.asList(new Integer[] {keyX, keyY-1})));                                                                                 
    	closeCells.add(map.get(Arrays.asList(new Integer[] {keyX+1, keyY-1})));                                                                               
                                                                                                                    
    	closeCells.add(map.get(Arrays.asList(new Integer[] {keyX-1, keyY})));                                                                                 
    	closeCells.add(map.get(Arrays.asList(new Integer[] {keyX+1, keyY})));                                                                                 

    	closeCells.add(map.get(Arrays.asList(new Integer[] {keyX-1, keyY+1})));                                                                               
    	closeCells.add(map.get(Arrays.asList(new Integer[] {keyX, keyY+1})));                                                                                 
    	closeCells.add(map.get(Arrays.asList(new Integer[] {keyX+1, keyY+1})));
    	
    	for (int i = 0; i < closeCells.size();i++){
    		if (closeCells.get(i)==null){
    			closeCells.remove(i);
    			i--;
    		}
    	}
    	return closeCells;                                                                                                                           
    }
    
    void moveAndUpdateHash(Particle particle, float x, float y){                                                                             
                                                                                                                                                    
    	List<Integer>oldKey=findCentralHashKey(particle);                                                                                           
                                                                                                                                                    
    	particle.x+=x;                                                                                                                              
    	particle.y+=y;
    	particle.compensateForContainerCollisions();                                                                                                
    	List<Integer>newKey=findCentralHashKey(particle);                                                                                           
                                                                                                                                                    
    	if(!oldKey.equals(newKey)){                                                                                                                 
    		if(map.containsKey(newKey)){                                                                                                            
    			map.get(newKey).contained.add(particle);                                                                                            
    			map.get(oldKey).contained.remove(particle);                                                                                         
    		}else{                                                                                                                                  
    			HashCell cell = new HashCell(newKey, interactionRadius);                                                            
    			cell.contained.add(particle);                                                                                                       
    			map.put(newKey, cell);                                                                                                              
    			map.get(oldKey).contained.remove(particle);                                                                                         
    		}                                                                                                                                       
			if(map.get(oldKey).contained.size()==0){                                                                                                
				map.remove(oldKey);                                                                                                                 
			}                                                                                                                                       
    	}                                                                                                                                           
    }                                                                                                                                               
                                                                                                                                                    
    float[] findDisplacementUnitVector(Particle from, Particle to){                                                                                             
    	float xDist = -(to.x-from.x);
    	float yDist = -(to.y-from.y);
    	float magnitude=(float) Math.sqrt((Math.pow(xDist, 2)+Math.pow(yDist, 2)));                                                                            
    	                                                                                                                                            
    	return new float[] {(float)xDist/magnitude, (float)yDist/magnitude};		
    }
    
    float[] findUnitVector(Particle from, float[] to){                                                                                             
    	float xDist = to[0]-from.x;
    	float yDist = to[1]-from.y;
    	float magnitude=(float) Math.sqrt((Math.pow(xDist, 2)+Math.pow(yDist, 2)));                                                                            
    	                                                                                                                                            
    	return new float[] {(float)xDist/magnitude, (float)yDist/magnitude};		
    }
    
    List<Integer> findCentralHashKey(Particle particle){
    	List<Integer> l=Arrays.asList(new Integer[] {(int)(particle.x/interactionRadius),(int)(particle.y/interactionRadius)});
    	return l;
    }
    
    List<Integer> findCentralHashKey(float[] point){
    	List<Integer> l=Arrays.asList(new Integer[] {(int)(point[0]/interactionRadius),(int)(point[1]/interactionRadius)});
    	return l;
    }
    
    List<Integer> findPairKey(Particle particle1, Particle particle2){
    	List<Integer> l;
    	if(particle1.idNum<particle2.idNum){
    		l=Arrays.asList(new Integer[] {particle1.idNum, particle2.idNum});
    	}else{
    		l=Arrays.asList(new Integer[] {particle2.idNum, particle1.idNum});
    	}
    	return l;
    }
    
    float findDistance(Particle particle1, Particle particle2){
    	return (float)Math.sqrt(Math.pow(particle1.x-particle2.x, 2)+Math.pow(particle1.y-particle2.y, 2));	
    }
    float findDistance(Particle particle1, float[] point){
    	return (float)Math.sqrt(Math.pow(particle1.x-point[0], 2)+Math.pow(particle1.y-point[1], 2));	
    }
    
    List<Particle> createNormalizedParticleList(Particle particle1, Particle particle2){
    	List<Particle> ps;
    	if(particle1.idNum<particle2.idNum){
    		ps=Arrays.asList(new Particle[] {particle1, particle2});
    	}else{
    		ps=Arrays.asList(new Particle[] {particle2, particle1});
    	}
    	return ps;
    }
    
    float findAngle(float[] vector){
    	float angle= (float) Math.atan(vector[1]/vector[0]);
    	if(vector[0]>=0){
    		if (!(vector[1]>=0)){
    			angle=(float) (Math.PI*2-angle);
    		}
    	}
    	else{
    		if (vector[1]>=0){
    			angle = (float) (Math.PI-angle);
    		}
    		else{
    			angle = (float) ((Math.PI*1.5f)-angle);
    		}
    	}
    	return angle;
    }

	void rotateUnitVector(float[] vector, float angle){
		float cosA=(float) Math.cos(angle);
		float sinA=(float) Math.sin(angle);
		float rx = vector[0]*cosA-vector[1]*sinA;
		float ry = vector[0]*sinA+vector[1]*cosA;
		vector[0]=rx;
		vector[1]=ry;
	}
	
	//a little off but good enough
		void setParticleLayout(){
			int particlesPerLine=(int) (xEdges/interactionRadius);
			
			int row = 1;
			
			int i = 0;
			//break label!
			finished:
			while(i<startCount){
				for (int pos = 0; pos<particlesPerLine;pos++){
					if(i>=startCount){
						break finished;
					}
					else{
						int offset;
						if (row%2==1){
							offset=(int) (xEdges%interactionRadius);
						}
						else{
							offset=0;
						}
						p.add(new Particle((float) safeLeft+interactionRadius*pos+offset, safeBottom-row*interactionRadius));
					}
					i++;
				}
				row++;
			}
			map=new HashMap<List<Integer>, HashCell>(p.size()); 
			for (int j=0;j<p.size();j++){   
				List<Integer> mapKey=findCentralHashKey(p.get(j));   
				if(map.containsKey(mapKey)){
					map.get(mapKey).contained.add(p.get(j));   
				}
				else{
					HashCell cell=new HashCell(mapKey, interactionRadius);
					map.put(mapKey, cell);
					cell.contained.add(p.get(j));  
				}
			}
			for (int j=0;j<p.size();j++){
				scene.attachChild(p.get(j).circle);
			}
		}
}
