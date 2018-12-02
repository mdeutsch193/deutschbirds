/*
 * Martin Deutsch
 * Honors Project - A Deep Reinforcement Learning Agent for Angry Birds
 * Colby College Computer Science Department, 2018
 */

package ab.deutschbirds;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import ab.demo.other.ActionRobot;
import ab.demo.other.Shot;
import ab.planner.TrajectoryPlanner;
import ab.utils.StateUtil;
import ab.vision.ABObject;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;

/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014, XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
 **  Sahan Abeyasinghe,Jim Keys,  Andrew Wang, Peng Zhang
 ** Modified by Martin Deutsch 2018
 ** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
 *****************************************************************************/

public class DeutschAgent implements Runnable {

	private ActionRobot aRobot;
	private Random randomGenerator;
	public int currentLevel = 1;
	public static int time_limit = 12;
	private Map<Integer,Integer> scores = new LinkedHashMap<Integer,Integer>();
	TrajectoryPlanner tp;
	private boolean firstShot;
	private Point prevTarget;
	private Process model;
	private BufferedWriter inputWriter;
	private BufferedReader outputReader;

	// a standalone implementation of the agent
	public DeutschAgent() {
		
		aRobot = new ActionRobot();
		tp = new TrajectoryPlanner();
		prevTarget = null;
		firstShot = true;
		randomGenerator = new Random();
		// start Python reinforcement learning model
		try {
			System.out.println("Starting Python model");
			ProcessBuilder pb = new ProcessBuilder("python3", "src/ab/deutschbirds/ShootingModel.py");
			model = pb.start();
			// writer for passing in data
			OutputStream stdin = model.getOutputStream();
			inputWriter = new BufferedWriter(new OutputStreamWriter(stdin));
			// reader for getting model output
			InputStream stdout = model.getInputStream();
			outputReader = new BufferedReader(new InputStreamReader(stdout));
		} catch (Throwable e) {
			System.out.println("Unable to start RL model");
		}
		// --- go to the Poached Eggs episode level selection page ---
		ActionRobot.GoFromMainMenuToLevelSelection();

	}

	
	// run the client
	public void run() {

		aRobot.loadLevel(currentLevel);
		while (true) {
			GameState state = solve();
			if (state == GameState.WON) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				int score = StateUtil.getScore(ActionRobot.proxy);
				if(!scores.containsKey(currentLevel))
					scores.put(currentLevel, score);
				else
				{
					if(scores.get(currentLevel) < score)
						scores.put(currentLevel, score);
				}
				int totalScore = 0;
				for(Integer key: scores.keySet()){

					totalScore += scores.get(key);
					System.out.println(" Level " + key
							+ " Score: " + scores.get(key) + " ");
				}
				System.out.println("Total Score: " + totalScore);
				aRobot.loadLevel(++currentLevel);
				// make a new trajectory planner whenever a new level is entered
				tp = new TrajectoryPlanner();

				// first shot on this level, try high shot first
				firstShot = true;
			} else if (state == GameState.LOST) {
				System.out.println("Restart");
				aRobot.restartLevel();
			} else if (state == GameState.LEVEL_SELECTION) {
				System.out
				.println("Unexpected level selection page, go to the last current level : "
						+ currentLevel);
				aRobot.loadLevel(currentLevel);
			} else if (state == GameState.MAIN_MENU) {
				System.out
				.println("Unexpected main menu page, go to the last current level : "
						+ currentLevel);
				ActionRobot.GoFromMainMenuToLevelSelection();
				aRobot.loadLevel(currentLevel);
			} else if (state == GameState.EPISODE_MENU) {
				System.out
				.println("Unexpected episode menu page, go to the last current level : "
						+ currentLevel);
				ActionRobot.GoFromMainMenuToLevelSelection();
				aRobot.loadLevel(currentLevel);
			}

		}

	}

	private double distance(Point p1, Point p2) {
		return Math
				.sqrt((double) ((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y)
						* (p1.y - p2.y)));
	}

	public GameState solve()
	{

		// capture Image
		BufferedImage screenshot = ActionRobot.doScreenShot();

		// process image
		Vision vision = new Vision(screenshot);

		// find the slingshot
		Rectangle sling = vision.findSlingshotMBR();

		// confirm the slingshot
		while (sling == null && aRobot.getState() == GameState.PLAYING) {
			System.out
			.println("No slingshot detected. Please remove pop up or zoom out");
			ActionRobot.fullyZoomOut();
			screenshot = ActionRobot.doScreenShot();
			vision = new Vision(screenshot);
			sling = vision.findSlingshotMBR();
		}
        // get all the pigs
 		List<ABObject> pigs = vision.findPigsMBR();

		GameState state = aRobot.getState();

		// if there is a sling, then play, otherwise just skip.
		if (sling != null) {

			if (!pigs.isEmpty()) {

				Point _tpt = null;
				Point releasePoint = null;
				Shot shot = new Shot();
				int dx,dy;

				try {
					System.out.println("Accessing Python model");
					String stateData = "";
					for (ABObject pig : pigs) {
						stateData += pig.getCenter().x + "," + pig.getCenter().y + ",";
					}
					for (int i = pigs.size(); i < 4; i++) {
						stateData += 0 + "," + 0 + ",";
					}
					// cycle through extraneous output
					while(outputReader.ready()) {
						outputReader.readLine();
					}
					System.out.println("State data: " + stateData);
					stateData += "\n";
					inputWriter.write(stateData);
					inputWriter.flush();
					String modelOutput = outputReader.readLine();
					System.out.println("Model Output: " + modelOutput);
					int chosenPig = Integer.parseInt(modelOutput);

					if (chosenPig >= pigs.size()) {
						chosenPig = 0;
					}
					_tpt = pigs.get(chosenPig).getCenter();
				}

				catch (Throwable e) {
					System.out.println("Error in Controller/Model pipeline");
					System.out.println("Aiming at random pig");
					_tpt = pigs.get(0).getCenter();
				}
				
 				// if the target is very close to before, randomly choose a
				// point near it
				if (prevTarget != null && distance(prevTarget, _tpt) < 10) {
					double _angle = randomGenerator.nextDouble() * Math.PI * 2;
					_tpt.x = _tpt.x + (int) (Math.cos(_angle) * 10);
					_tpt.y = _tpt.y + (int) (Math.sin(_angle) * 10);
					System.out.println("Randomly changing to " + _tpt);
				}

				prevTarget = new Point(_tpt.x, _tpt.y);

				// estimate the trajectory
				ArrayList<Point> pts = tp.estimateLaunchPoint(sling, _tpt);

				// do a high shot when entering a level to find an accurate velocity
				if (firstShot && pts.size() > 1)
				{
					releasePoint = pts.get(1);
				}
				else if (pts.size() == 1)
					releasePoint = pts.get(0);
				else if (pts.size() == 2)
				{
					// randomly choose between the trajectories, with a 1 in
					// 6 chance of choosing the high one
					if (randomGenerator.nextInt(6) == 0)
						releasePoint = pts.get(1);
					else
						releasePoint = pts.get(0);
				}
				else
					if(pts.isEmpty())
					{
						System.out.println("No release point found for the target");
						System.out.println("Try a shot with 45 degree");
						releasePoint = tp.findReleasePoint(sling, Math.PI/4);
					}

				// Get the reference point
				Point refPoint = tp.getReferencePoint(sling);


				//Calculate the tapping time according the bird type
				if (releasePoint != null) {
					double releaseAngle = tp.getReleaseAngle(sling,
							releasePoint);
					System.out.println("Release Point: " + releasePoint);
					System.out.println("Release Angle: "
							+ Math.toDegrees(releaseAngle));
					int tapInterval = 0;
					switch (aRobot.getBirdTypeOnSling())
					{

					case RedBird:
						tapInterval = 0; break;               // start of trajectory
					case YellowBird:
						tapInterval = 65 + randomGenerator.nextInt(25);break; // 65-90% of the way
					case WhiteBird:
						tapInterval =  70 + randomGenerator.nextInt(20);break; // 70-90% of the way
					case BlackBird:
						tapInterval =  70 + randomGenerator.nextInt(20);break; // 70-90% of the way
					case BlueBird:
						tapInterval =  65 + randomGenerator.nextInt(20);break; // 65-85% of the way
					default:
						tapInterval =  60;
					}

					int tapTime = tp.getTapTime(sling, releasePoint, _tpt, tapInterval);
					dx = (int)releasePoint.getX() - refPoint.x;
					dy = (int)releasePoint.getY() - refPoint.y;
					shot = new Shot(refPoint.x, refPoint.y, dx, dy, 0, tapTime);
				}
				else
					{
						System.err.println("No Release Point Found");
						return state;
					}
				// check whether the slingshot is changed. the change of the slingshot indicates a change in the scale.
				ActionRobot.fullyZoomOut();
				screenshot = ActionRobot.doScreenShot();
				vision = new Vision(screenshot);
				Rectangle _sling = vision.findSlingshotMBR();
				if(_sling != null)
				{
					double scale_diff = Math.pow((sling.width - _sling.width),2) +  Math.pow((sling.height - _sling.height),2);
					if(scale_diff < 25)
					{
						if(dx < 0)
						{
							aRobot.cshoot(shot);
							// send score to model as reward
							try {
								int score = StateUtil.getScore(ActionRobot.proxy);
								System.out.println("Reward = " + score);
								inputWriter.write(Integer.toString(score) + "\n");
								inputWriter.flush();
							} catch (Throwable e) {
								System.out.println("Unable to pass score to model");
							}
							state = aRobot.getState();
							if ( state == GameState.PLAYING )
							{
								screenshot = ActionRobot.doScreenShot();
								vision = new Vision(screenshot);
								List<Point> traj = vision.findTrajPoints();
								tp.adjustTrajectory(traj, sling, releasePoint);
								firstShot = false;
							}
						}
					}
					else
						System.out.println("Scale is changed, can not execute the shot, will re-segement the image");
						state = aRobot.getState();
				}
				else
					System.out.println("No sling detected, can not execute the shot, will re-segement the image");
					state = aRobot.getState();
			}

		}
		return state;
	}

	public static void main(String args[]) {

		DeutschAgent agent = new DeutschAgent();
		if (args.length > 0)
			agent.currentLevel = Integer.parseInt(args[0]);
		agent.run();

	}
}
