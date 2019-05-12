# A Deep Reinforcement Learning Agent for Angry Birds
This project contains the code for two reinforcement learning agents for the game Angry Birds. The first is a Deep Deterministic Policy Gradients (DDPG) algorithm, inspired by [Enhancing Deep Reinforcement Learning Agent for Angry Birds](https://aibirds.org/2017/aibirds_BNU.pdf) (Yuan et al., 2017). The second is a Deep Q-Network, developed for comparison with the DDPG.
## Installation Instructions
The Angry Birds agent runs on the Chrome version of Angry Birds. The online version of Angry Birds Chrome has been discontinued, so to run the agent an offline version must be installed. 
1. Download [Chromium v67](https://www.chromium.org/getting-involved/download-chromium)
2. Go to the directory:
   - For Windows: *C:\\Users\$your_user_profile$\AppData\Local\Chromium\User Data\Default*
   - For Linux: */home/$your_user_profile$/.config/Chromium/Profile 1/*
   - For Mac: */Users/$your_user_profile$/Library/Application Support/Chromium/Default*
3. Replace the Application Cache folder with the included Application Cache folder 
4. Restart Chromium and access the game by loading chrome.angrybirds.com
5. Install the Angry Birds Interface plugin
   - Go to Window->Extensions
   - Make sure Developer Mode is enabled and click "Load Unpacked Extension"
   - Select the included "Plugin" folder

## Running the Game Playing Software
The project can be easily compiled and run using [Apache Ant](https://ant.apache.org/), so that should be installed if it is not already. To compile the project, execute
```
ant compile
ant jar
```
To run the project, make sure the offline Chromium version of Angry Birds is running and the Angry Birds Interface plugin is installed, then execute
```
ant run
```
### Switching Between Reinforcement Learning Agents
To switch from the DDPG agent to the DQN agent, navigate to *src/ab/deutschbirds/DeutschAgent.java* and replace `src/ab/deutschbirds/ShootingModel.py` on line 75 with `src/ab/deutschbirds/ShootingModelDQN.py`.

## Acknowledgements
The software for interacting with the Angry Birds game came from the [AIBirds](https://aibirds.org/) competition, sponsored by the Australian National University. The outline for my DDPG agent came from [Patrick Emami's DDPG](https://pemami4911.github.io/blog/2016/08/21/ddpg-rl.html) for OpenAI Gym Pendulum. The outline for my DQN came from [Keon Kim's DQN](https://github.com/keon/deep-q-learning) for OpenAI Gym CartPole.
Finally, I'd like to thank my advisor, Bruce Maxwell, for guiding me through the process of building this project and allowing me to pursue such a unique and interesting application of reinforcement learning.
