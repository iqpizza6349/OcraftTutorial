package com.tistory.workshop6349;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.S2Coordinator;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.game.Difficulty;
import com.github.ocraft.s2client.protocol.game.LocalMap;
import com.github.ocraft.s2client.protocol.game.Race;
import com.github.ocraft.s2client.protocol.unit.Unit;

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

        @Override
        public void onUnitIdle(UnitInPool unitInPool) {
            Unit unit = unitInPool.unit(); // 아직도 이해 못함 원리를
            switch ((Units) unit.getType()) {
                case TERRAN_COMMAND_CENTER:
                    actions().unitCommand(unit, Abilities.TRAIN_SCV, false);
                    break;
                default:
                    break;
            }
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
