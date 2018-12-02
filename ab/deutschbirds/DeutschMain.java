/*
 * Martin Deutsch
 * Honors Project - A Deep Reinforcement Learning Agent for Angry Birds
 * Colby College Computer Science Department, 2018
 */

package ab.deutschbirds;

import ab.planner.abTrajectory;
import ab.utils.GameImageRecorder;
import ab.vision.ShowSeg;

/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014, XiaoYu (Gary) Ge, Jochen Renz,Stephen Gould,
 **  Sahan Abeyasinghe,Jim Keys,  Andrew Wang, Peng Zhang
 ** Modified by Martin Deutsch 2018
 ** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
 *****************************************************************************/

public class DeutschMain {
	// the entry of the software.
	public static void main(String args[])
	{
		String command = "";
		if(args.length > 0)
		{
			command = args[0];
			if (args.length == 1 && command.equalsIgnoreCase("-na"))
			{
				DeutschAgent agent = new DeutschAgent();
				agent.run();
			}
			else
				if(command.equalsIgnoreCase("-cshoot"))
				{
					ShootingAgent.shoot(args, true);
				}
				else
					if(command.equalsIgnoreCase("-pshoot"))
					{
						ShootingAgent.shoot(args, false);
					}

					else	
						if (args.length == 2 && command.equalsIgnoreCase("-na"))
						{
							DeutschAgent agent = new DeutschAgent();
							if(! (args[1].equalsIgnoreCase("-showMBR") || args[1].equals("-showReal")))
							{
								int initialLevel = 1;
								try{
									initialLevel = Integer.parseInt(args[1]);
								}
								catch (NumberFormatException e)
								{
									System.out.println("wrong level number, will use the default one");
								}
								agent.currentLevel = initialLevel;
								agent.run();
							}
							else
							{
								Thread agentThread = new Thread(agent);
								agentThread.start();										   
								if(args[1].equalsIgnoreCase("-showReal"))
									ShowSeg.useRealshape = true;
								Thread thre = new Thread(new ShowSeg());
								thre.start();
							}
						} 
						else if (args.length == 3 && (args[2].equalsIgnoreCase("-showMBR") || args[2].equalsIgnoreCase("-showReal")) && command.equalsIgnoreCase("-na"))
						{
							DeutschAgent agent = new DeutschAgent();
							int initialLevel = 1;
							try{
								initialLevel = Integer.parseInt(args[1]);
							}
							catch (NumberFormatException e)
							{
								System.out.println("wrong level number, will use the default one");
							}
							agent.currentLevel = initialLevel;
							Thread agentThread = new Thread(agent);
							agentThread.start();
							if(args[2].equalsIgnoreCase("-showReal"))
								ShowSeg.useRealshape = true;
							Thread thre = new Thread(new ShowSeg());
							thre.start();

						}

						else if(command.equalsIgnoreCase("-showMBR"))
						{
							ShowSeg showseg = new ShowSeg();
							showseg.run();						
						}
						else if (command.equalsIgnoreCase("-showReal"))
						{
							ShowSeg showseg = new ShowSeg();
							ShowSeg.useRealshape = true;
							showseg.run();
						}
						else if (command.equalsIgnoreCase("-showTraj"))
						{
							String[] param = {};
							abTrajectory.main(param);
						} 
						else if (command.equalsIgnoreCase("-recordImg"))
						{

							if(args.length < 2)
								System.out.println("please specify the directory");
							else
							{
								String[] param = {args[1]};   

								GameImageRecorder.main(param);
							}
						}
						else 
							System.out.println("Please input the correct command");
		}
		else 
			System.out.println("Please input the correct command");
		

	}
}
