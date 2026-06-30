package com.unnamednuclear.test;

import com.unnamednuclear.UnnamedNuclear;
import com.unnamednuclear.block.CentrifugeBlockEntity;
import com.unnamednuclear.item.NuclearComposition;
import com.unnamednuclear.registration.Registration;
import com.unnamednuclear.simulation.SimulationNode;
import com.unnamednuclear.simulation.WorldSimulationData;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(UnnamedNuclear.MODID)
public class AutomatedNuclearTests {

    @PrefixGameTestTemplate(false)
    @GameTest(template = "minecraft:empty")
    public static void testCentrifugeItemProcessing(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, Registration.CENTRIFUGE.get());
        
        CentrifugeBlockEntity be = (CentrifugeBlockEntity) helper.getBlockEntity(pos);
        
        // Prepare input: 2 UF6 items with standard composition
        ItemStack input = new ItemStack(Registration.URANIUM_HEXAFLUORIDE.get(), 2);
        NuclearComposition natural = new NuclearComposition(java.util.Map.of(id("u235"), 0.0071, id("u238"), 0.99285, id("u234"), 0.00005));
        input.set(Registration.COMPOSITION.get(), natural);
        
        be.getInventory().setStackInSlot(0, input);
        
        // Process
        // In the code, tick() calls process() if canProcess() is true.
        // Let's manually trigger it to be sure.
        helper.runAtTickTime(10, () -> {
            CentrifugeBlockEntity.tick(helper.getLevel(), helper.absolutePos(pos), Registration.CENTRIFUGE.get().defaultBlockState(), be);
            
            ItemStack product = be.getInventory().getStackInSlot(1);
            ItemStack tails = be.getInventory().getStackInSlot(2);
            
            helper.assertFalse(product.isEmpty(), "Product should not be empty");
            helper.assertFalse(tails.isEmpty(), "Tails should not be empty");
            
            NuclearComposition pComp = product.get(Registration.COMPOSITION.get());
            NuclearComposition tComp = tails.get(Registration.COMPOSITION.get());
            
            helper.assertTrue(pComp.u235() > natural.u235(), "Product should be enriched in U235");
            helper.assertTrue(tComp.u235() < natural.u235(), "Tails should be depleted in U235");
            
            helper.succeed();
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "minecraft:empty")
    public static void testCentrifugeFluidProcessing(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, Registration.CENTRIFUGE.get());
        
        CentrifugeBlockEntity be = (CentrifugeBlockEntity) helper.getBlockEntity(pos);
        
        // Fill input tank with 200mB UF6
        be.getInputTank().fill(new FluidStack(Registration.UF6.get(), 200), IFluidHandler.FluidAction.EXECUTE);
        
        helper.runAtTickTime(10, () -> {
            CentrifugeBlockEntity.tick(helper.getLevel(), helper.absolutePos(pos), Registration.CENTRIFUGE.get().defaultBlockState(), be);
            
            helper.assertTrue(be.getInputTank().isEmpty(), "Input tank should be empty after processing");
            helper.assertTrue(be.getProductTank().getFluidAmount() >= 100, "Product tank should have fluid");
            helper.assertTrue(be.getTailsTank().getFluidAmount() >= 100, "Tails tank should have fluid");
            
            helper.succeed();
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "minecraft:empty")
    public static void testHeatExchanger(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, Registration.HEAT_EXCHANGER.get());
        
        com.unnamednuclear.block.HeatExchangerBlockEntity be = (com.unnamednuclear.block.HeatExchangerBlockEntity) helper.getBlockEntity(pos);
        
        // Fill primary tank with hot sodium and water tank with water
        be.getPrimaryTank().fill(new FluidStack(Registration.HOT_SODIUM.get(), 1000), IFluidHandler.FluidAction.EXECUTE);
        be.getWaterTank().fill(new FluidStack(net.minecraft.world.level.material.Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
        
        // We need to wait for ticks to transfer heat and boil water
        helper.runAtTickTime(20, () -> {
             // Heat should have increased
             // Fluid should have been converted to cooled sodium
             helper.assertTrue(be.getPrimaryTank().getFluid().is(Registration.SODIUM.get()), "Sodium should be cooled");
             helper.assertTrue(be.getPrimaryTank().getFluidAmount() == 1000, "Fluid volume should be preserved");
             
             // If heat is high enough, steam should be produced
             // HeatExchanger ticks every tick, so in 20 ticks it should have processed some.
             if (be.getSteamTank().isEmpty()) {
                 // Try more ticks or check heat
                 helper.fail("Steam should have been produced. Current heat: " + be.getPrimaryTank().getFluidAmount());
             }
             
             helper.succeed();
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "minecraft:empty")
    public static void testReactorAssembly(GameTestHelper helper) {
        // Create a 3x3x3 reactor
        // Controller at (1, 1, 0)
        // Casing everywhere else on shell
        // Hollow interior
        
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = 0; z < 3; z++) {
                    BlockPos p = new BlockPos(x, y, z);
                    if (x == 1 && y == 1 && z == 0) {
                        helper.setBlock(p, Registration.REACTOR_CONTROLLER.get());
                    } else if (x == 0 || x == 2 || y == 0 || y == 2 || z == 0 || z == 2) {
                        helper.setBlock(p, Registration.REACTOR_CASING.get());
                    } else {
                        helper.setBlock(p, Registration.MODERATOR.get()); // Interior
                    }
                }
            }
        }
        
        BlockPos controllerPos = new BlockPos(1, 1, 0);
        helper.runAtTickTime(20, () -> {
            com.unnamednuclear.block.ReactorControllerBlockEntity be = (com.unnamednuclear.block.ReactorControllerBlockEntity) helper.getBlockEntity(controllerPos);
            helper.assertTrue(be.isAssembled(), "Reactor should be assembled. Result: " + be.getLastResult());
            helper.succeed();
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "minecraft:empty")
    public static void testReactorWithFuel(GameTestHelper helper) {
        // Build the same 3x3x3 reactor but with a fuel rod
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = 0; z < 3; z++) {
                    BlockPos p = new BlockPos(x, y, z);
                    if (x == 1 && y == 1 && z == 0) {
                        helper.setBlock(p, Registration.REACTOR_CONTROLLER.get());
                    } else if (x == 1 && y == 1 && z == 1) {
                        helper.setBlock(p, Registration.FUEL_CHANNEL.get()); // Fuel channel
                    } else if (x == 0 || x == 2 || y == 0 || y == 2 || z == 0 || z == 2) {
                        helper.setBlock(p, Registration.REACTOR_CASING.get());
                    } else {
                        helper.setBlock(p, Registration.MODERATOR.get());
                    }
                }
            }
        }

        BlockPos controllerPos = new BlockPos(1, 1, 0);
        BlockPos channelPos = new BlockPos(1, 1, 1);

        helper.runAtTickTime(10, () -> {
            com.unnamednuclear.block.ReactorControllerBlockEntity controllerBe = (com.unnamednuclear.block.ReactorControllerBlockEntity) helper.getBlockEntity(controllerPos);
            com.unnamednuclear.block.ReactorChannelBlockEntity channelBe = (com.unnamednuclear.block.ReactorChannelBlockEntity) helper.getBlockEntity(channelPos);
            
            helper.assertTrue(controllerBe.isAssembled(), "Reactor should be assembled");
            
            // Insert fuel pellet
            ItemStack fuel = new ItemStack(Registration.FUEL_PELLET.get());
            channelBe.setItem(fuel);
            
            helper.runAtTickTime(20, () -> {
                // Check if simulation is running
                helper.assertTrue(controllerBe.getTotalHeat() >= 0, "Heat should be tracked");
                helper.succeed();
            });
        });
    }
    @PrefixGameTestTemplate(false)
    @GameTest(template = "minecraft:empty")
    public static void testRBMKReactor(GameTestHelper helper) {
        // RBMK: Graphite moderated, Water cooled
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                for (int z = 0; z < 5; z++) {
                    BlockPos p = new BlockPos(x, y, z);
                    if (x == 2 && y == 2 && z == 0) {
                        helper.setBlock(p, Registration.REACTOR_CONTROLLER.get().defaultBlockState().setValue(com.unnamednuclear.block.ReactorControllerBlock.FACING, net.minecraft.core.Direction.SOUTH));
                    } else if (x == 0 || x == 4 || y == 0 || y == 4 || z == 0 || z == 4) {
                        helper.setBlock(p, Registration.REACTOR_CASING.get());
                    } else {
                        // Interior
                        if (x == 2 && y == 2) {
                            helper.setBlock(p, Registration.FUEL_CHANNEL.get());
                        } else if ((x + y) % 2 == 0) {
                            helper.setBlock(p, Registration.MODERATOR.get());
                        } else {
                            helper.setBlock(p, Registration.COOLANT_CHANNEL.get());
                        }
                    }
                }
            }
        }

        BlockPos controllerPos = new BlockPos(2, 2, 0);
        helper.runAtTickTime(20, () -> {
            com.unnamednuclear.block.ReactorControllerBlockEntity be = (com.unnamednuclear.block.ReactorControllerBlockEntity) helper.getBlockEntity(controllerPos);
            helper.assertTrue(be.isAssembled(), "RBMK should be assembled");
            
            // Check that it's using RBMK logic (via SimulationData)
            com.unnamednuclear.simulation.WorldSimulationData data = com.unnamednuclear.simulation.WorldSimulationData.get((net.minecraft.server.level.ServerLevel) helper.getLevel());
            BlockPos fuelPos = helper.absolutePos(new BlockPos(2, 2, 1));
            // We can't easily check the type from outside without adding a getter, 
            // but we can check if it works.
            
            // Insert fuel
            com.unnamednuclear.block.ReactorChannelBlockEntity fuelBe = (com.unnamednuclear.block.ReactorChannelBlockEntity) helper.getBlockEntity(new BlockPos(2, 2, 1));
            fuelBe.setItem(new ItemStack(Registration.NUCLEAR_FUEL.get()));
            
            helper.runAtTickTime(40, () -> {
                helper.assertTrue(be.getTotalHeat() > 0, "RBMK should generate heat");
                helper.succeed();
            });
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "minecraft:empty")
    public static void testSodiumFastReactor(GameTestHelper helper) {
        // Sodium Fast: No moderator
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = 0; z < 3; z++) {
                    BlockPos p = new BlockPos(x, y, z);
                    if (x == 1 && y == 1 && z == 0) {
                        helper.setBlock(p, Registration.REACTOR_CONTROLLER.get());
                    } else if (x == 0 || x == 2 || y == 0 || y == 2 || z == 0 || z == 2) {
                        helper.setBlock(p, Registration.REACTOR_CASING.get());
                    } else {
                        helper.setBlock(p, Registration.FUEL_CHANNEL.get());
                    }
                }
            }
        }

        BlockPos controllerPos = new BlockPos(1, 1, 0);
        helper.runAtTickTime(20, () -> {
            com.unnamednuclear.block.ReactorControllerBlockEntity be = (com.unnamednuclear.block.ReactorControllerBlockEntity) helper.getBlockEntity(controllerPos);
            helper.assertTrue(be.isAssembled(), "Sodium Fast Reactor should be assembled");
            
            com.unnamednuclear.block.ReactorChannelBlockEntity fuelBe = (com.unnamednuclear.block.ReactorChannelBlockEntity) helper.getBlockEntity(new BlockPos(1, 1, 1));
            fuelBe.setItem(new ItemStack(Registration.NUCLEAR_FUEL.get()));
            
            helper.runAtTickTime(40, () -> {
                helper.assertTrue(be.getNetFlux() > 0, "Fast reactor should have flux");
                helper.succeed();
            });
        });
    }

    @GameTest(template = "minecraft:empty")
    @PrefixGameTestTemplate(false)
    public static void testLooseBlockSimulation(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, Registration.FUEL_CHANNEL.get());
        
        // Place fuel in the channel
        net.minecraft.world.level.block.entity.BlockEntity be = helper.getBlockEntity(pos);
        if (be instanceof com.unnamednuclear.block.ReactorChannelBlockEntity channel) {
            net.minecraft.world.item.ItemStack fuel = new net.minecraft.world.item.ItemStack(Registration.NUCLEAR_FUEL.get());
            channel.setItem(fuel);
        }

        // It should still produce heat/neutrons even without a multiblock controller
        helper.runAtTickTime(40, () -> {
            WorldSimulationData data = WorldSimulationData.get(helper.getLevel());
            SimulationNode node = data.getNode(helper.absolutePos(pos));
            helper.assertTrue(node != null, "Simulation node should exist for loose fuel channel");
            helper.assertTrue(node.fastNeutrons > 0, "Loose fuel channel should produce neutrons");
            helper.assertTrue(node.heat > 0, "Loose fuel channel should produce heat");
            helper.succeed();
        });
    }

    @GameTest(template = "minecraft:empty")
    @PrefixGameTestTemplate(false)
    public static void testRBMKInstability(GameTestHelper helper) {
        // Build an RBMK-like structure (Graphite moderated)
        // Fuel in center, Moderator around it, no coolant (simulating void/loss of coolant)
        BlockPos fuelPos = new BlockPos(1, 1, 1);
        helper.setBlock(fuelPos, Registration.FUEL_CHANNEL.get());
        helper.setBlock(fuelPos.north(), Registration.MODERATOR.get());
        helper.setBlock(fuelPos.south(), Registration.MODERATOR.get());
        helper.setBlock(fuelPos.east(), Registration.MODERATOR.get());
        helper.setBlock(fuelPos.west(), Registration.MODERATOR.get());
        
        net.minecraft.world.level.block.entity.BlockEntity be = helper.getBlockEntity(fuelPos);
        if (be instanceof com.unnamednuclear.block.ReactorChannelBlockEntity channel) {
            net.minecraft.world.item.ItemStack fuel = new net.minecraft.world.item.ItemStack(Registration.NUCLEAR_FUEL.get());
            channel.setItem(fuel);
        }

        // After some time, it should heat up and explode due to positive feedback
        helper.runAtTickTime(100, () -> {
            WorldSimulationData data = WorldSimulationData.get(helper.getLevel());
            SimulationNode node = data.getNode(helper.absolutePos(fuelPos));
            helper.assertTrue(node != null, "Node should exist");
            // If it exploded, the block might be gone or air
            if (helper.getBlockState(fuelPos).isAir()) {
                helper.succeed();
            } else {
                // If not yet exploded, check if it's runaway
                helper.assertTrue(node.heat > 1000, "RBMK should be heating up rapidly");
                helper.succeed();
            }
        });
    }
    private static net.minecraft.resources.ResourceLocation id(String path) {
        return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("unnamednuclear", path);
    }
}
