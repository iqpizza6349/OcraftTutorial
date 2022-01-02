package com.tistory.workshop6349;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.S2Coordinator;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Ability;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.game.BattlenetMap;
import com.github.ocraft.s2client.protocol.game.Difficulty;
import com.github.ocraft.s2client.protocol.game.LocalMap;
import com.github.ocraft.s2client.protocol.game.Race;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Unit;
import io.netty.util.internal.ThreadLocalRandom;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class TutorialBot {

    private static class Bot extends S2Agent {

        @Override
        public void onGameStart() {
            System.out.println("Hello world of Starcraft II bots!");
        }

        @Override
        public void onStep() {
            tryBuildSupplyDepot();
            tryBuildBarracks();
        }

        private boolean tryBuildSupplyDepot() {
            // If we are not supply capped, don't build a supply depot.
            if (observation().getFoodUsed() <= observation().getFoodCap() - 2) {
                return false;
            }

            // Try and build a depot. Find a random TERRAN_SCV and give it the order.
            return tryBuildStructure(Abilities.BUILD_SUPPLY_DEPOT, Units.TERRAN_SCV);
        }

        private boolean tryBuildStructure(Ability abilityTypeForStructure, UnitType unitType) {
            // If a unit already is building a supply structure of this type, do nothing.
            if (!observation().getUnits(Alliance.SELF, doesBuildWith(abilityTypeForStructure)).isEmpty()) {
                return false;
            }

            // Just try a random location near the unit.
            Optional<UnitInPool> unitInPool = getRandomUnit(unitType);
            if (unitInPool.isPresent()) {
                Unit unit = unitInPool.get().unit();
                actions().unitCommand(
                        unit,
                        abilityTypeForStructure,
                        unit.getPosition().toPoint2d().add(Point2d.of(getRandomScalar(), getRandomScalar()).mul(15.0f)),
                        false);
                return true;
            } else {
                return false;
            }
        }

        private boolean tryBuildBarracks() {
            if (countUnitType(Units.TERRAN_SUPPLY_DEPOT) < 1) {
                return false;
            }

            if (countUnitType(Units.TERRAN_BARRACKS) > 0) {
                return false;
            }

            return tryBuildStructure(Abilities.BUILD_BARRACKS, Units.TERRAN_SCV);
        }

        private Predicate<UnitInPool> doesBuildWith(Ability abilityTypeForStructure) {
            return unitInPool -> unitInPool.unit()
                    .getOrders()
                    .stream()
                    .anyMatch(unitOrder -> abilityTypeForStructure.equals(unitOrder.getAbility()));
        }

        private Optional<UnitInPool> getRandomUnit(UnitType unitType) {
            List<UnitInPool> units = observation().getUnits(Alliance.SELF, UnitInPool.isUnit(unitType));
            return units.isEmpty()
                    ? Optional.empty()
                    : Optional.of(units.get(ThreadLocalRandom.current().nextInt(units.size())));
        }

        private float getRandomScalar() {
            return ThreadLocalRandom.current().nextFloat() * 2 - 1;
        }


        @Override
        public void onUnitIdle(UnitInPool unitInPool) {
            Unit unit = unitInPool.unit();
            switch ((Units) unit.getType()) {
                case TERRAN_COMMAND_CENTER:
                    actions().unitCommand(unit, Abilities.TRAIN_SCV, false);
                    break;
                case TERRAN_SCV:
                    findNearestMineralPatch(unit.getPosition().toPoint2d()).ifPresent(mineralPath ->
                            actions().unitCommand(unit, Abilities.SMART, mineralPath, false));
                    break;
                case TERRAN_BARRACKS: {
                    actions().unitCommand(unit, Abilities.TRAIN_MARINE, false);
                    break;
                }
                default:
                    break;
            }
        }

        private Optional<Unit> findNearestMineralPatch(Point2d start) {
            List<UnitInPool> units = observation().getUnits(Alliance.NEUTRAL);
            double distance = Double.MAX_VALUE;
            Unit target = null;
            for (UnitInPool unitInPool : units) {
                Unit unit = unitInPool.unit();
                if (unit.getType().equals(Units.NEUTRAL_MINERAL_FIELD)) {
                    double d = unit.getPosition().toPoint2d().distance(start);
                    if (d < distance) {
                        distance = d;
                        target = unit;
                    }
                }
            }
            return Optional.ofNullable(target);
        }

        private int countUnitType(Units unitType) {
            return observation().getUnits(Alliance.SELF, UnitInPool.isUnit(unitType)).size();
        }


    }

    public static void main(String[] args) {
        Bot bot = new Bot();
        S2Coordinator s2Coordinator = S2Coordinator.setup()
                .loadSettings(args)
                .setParticipants(
                        S2Coordinator.createParticipant(Race.TERRAN, bot),
                        S2Coordinator.createComputer(Race.ZERG, Difficulty.VERY_EASY))
                .launchStarcraft()
                .startGame(LocalMap.of(Path.of("sc2ai_2022_season1/2000AtmospheresAIE.SC2Map")));

        while (s2Coordinator.update()) {
        }

        s2Coordinator.quit();
    }
}
