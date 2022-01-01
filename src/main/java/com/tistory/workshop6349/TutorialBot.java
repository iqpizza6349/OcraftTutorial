package com.tistory.workshop6349;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.S2Coordinator;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Ability;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.game.Difficulty;
import com.github.ocraft.s2client.protocol.game.LocalMap;
import com.github.ocraft.s2client.protocol.game.Race;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Unit;
import io.netty.util.internal.ThreadLocalRandom;

import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class TutorialBot {

    private static class Bot extends S2Agent {
        @Override
        public void onGameStart() {
            System.out.println("Hello, World of StarCraft2 bots!");
        }

        @Override
        public void onStep() {
            tryBuildSupplyDepot();
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

        private boolean tryBuildSupplyDepot() {
            if (observation().getFoodUsed() <= observation().getFoodCap() - 2) {
                return false;
            }

            return tryBuildStructure(Abilities.BUILD_SUPPLY_DEPOT, Units.TERRAN_SCV);
        }

        private boolean tryBuildStructure(Abilities abilities, UnitType type) {
            if (!observation().getUnits(Alliance.SELF, doesBuildWith(abilities)).isEmpty()) {
                return false;
            }

            Optional<UnitInPool> unitInPool = getRandomUnit(type);
            if (unitInPool.isPresent()) {
                Unit unit = unitInPool.get().unit();
                actions().unitCommand(
                        unit,
                        abilities,
                        unit.getPosition().toPoint2d().add(Point2d.of(getRandomScalar(), getRandomScalar()).mul(15f)),
                        false
                );
                return true;
            }
            else {
                return false;
            }
        }

        private Predicate<UnitInPool> doesBuildWith(Ability ability) {
            return unitInPool -> unitInPool.unit()
                    .getOrders()
                    .stream()
                    .anyMatch(unitOrder -> ability.equals(unitOrder.getAbility()));
        }

        private Optional<UnitInPool> getRandomUnit(UnitType type) {
            List<UnitInPool> units = observation().getUnits(Alliance.SELF, UnitInPool.isUnit(type));
            return units.isEmpty()
                    ? Optional.empty()
                    : Optional.of(units.get(ThreadLocalRandom.current().nextInt(units.size())));
        }

        private float getRandomScalar() {
            return ThreadLocalRandom.current().nextFloat() * 2 - 1;
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
