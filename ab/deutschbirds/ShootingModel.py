# Martin Deutsch
# Honors Project - A Deep Reinforcement Learning Agent for Angry Birds
# Colby College Computer Science Department, 2018

import random
import numpy as np
from collections import deque
from keras.models import Sequential
from keras.layers import Dense
from keras.optimizers import Adam

# Deep Q-learning Agent (https://keon.io/deep-q-learning/)
class DQNAgent:
    def __init__(self, state_size, action_size):
        self.state_size = state_size
        self.action_size = action_size
        self.memory = deque(maxlen=2000)
        self.gamma = 0.95    # discount rate
        self.epsilon = 1.0  # exploration rate
        self.epsilon_min = 0.01
        self.epsilon_decay = 0.995
        self.learning_rate = 0.001
        self.model = self._build_model()

    def _build_model(self):
        # Neural Net for Deep-Q learning Model
        model = Sequential()
        model.add(Dense(24, input_dim=self.state_size, activation='relu'))
        model.add(Dense(24, activation='relu'))
        model.add(Dense(self.action_size, activation='linear'))
        model.compile(loss='mse',
                      optimizer=Adam(lr=self.learning_rate))
        return model

    def remember(self, state, action, reward, next_state, done):
        self.memory.append((state, action, reward, next_state, done))

    def act(self, state):
        if np.random.rand() <= self.epsilon:
            return random.randrange(self.action_size)
        act_values = self.model.predict(state)
        return np.argmax(act_values[0])  # returns action

    def replay(self, batch_size):
        minibatch = random.sample(self.memory, batch_size)
        for state, action, reward, next_state, done in minibatch:
            target = reward
            if not done:
                target = (reward + self.gamma *
                          np.amax(self.model.predict(next_state)[0]))
            target_f = self.model.predict(state)
            target_f[0][action] = target
            self.model.fit(state, target_f, epochs=1, verbose=0)
        if self.epsilon > self.epsilon_min:
            self.epsilon *= self.epsilon_decay

    def load(self, name):
        self.model.load_weights(name)

    def save(self, name):
        self.model.save_weights(name)
        
if __name__ == "__main__":
	# take in x and y positions of 4 pigs
    state_size = 8
    # output pig number (0-4)
    action_size = 4
    agent = DQNAgent(state_size, action_size)
    done = False
    batch_size = 32
    
    pigs = input()
    state = pigs.split(',')
    
    while True:	
    	action = agent.act(state)
    	print(action)
    	reward = input()
    	next_state = input()
    	next_state = next_state.split(',')
    	agent.remember(state, action, reward, next_state, done)
    	state = next_state
    	if len(agent.memory) > batch_size:
    		agent.replay(batch_size)