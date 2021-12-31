package com.tistory.workshop6349;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.S2Coordinator;
import com.github.ocraft.s2client.protocol.game.Difficulty;
import com.github.ocraft.s2client.protocol.game.LocalMap;
import com.github.ocraft.s2client.protocol.game.Race;

import java.nio.file.Paths;

public class TutorialBot {

    private static class Bot extends S2Agent {
        @Override
        public void onGameStart() {
            System.out.println("Hello, World of StarCraft2 bots!");
        }

        @Override
        public void onStep() {
            System.out.println(observation().getGameLoop());
        }
    }

    public static void main(String[] args) {
        Bot bot = new Bot();
        S2Coordinator s2Coordinator = S2Coordinator.setup()
                .loadSettings(args)
                .setParticipants(
                        S2Coordinator.createParticipant(Race.TERRAN, bot),
                        S2Coordinator.createComputer(Race.ZERG, Difficulty.VERY_EASY)
                )
                .launchStarcraft()
                .startGame(LocalMap.of(Paths.get("2000AtmospheresAIE.SC2Map")));

        while (s2Coordinator.update()) {

        }

        s2Coordinator.quit();
    }
}
