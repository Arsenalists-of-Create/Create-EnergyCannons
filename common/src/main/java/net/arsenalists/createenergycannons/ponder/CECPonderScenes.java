package net.arsenalists.createenergycannons.ponder;

import net.arsenalists.createenergycannons.content.cannons.magnetic.coilgun.CoilGunBlock;
import net.arsenalists.createenergycannons.content.cannons.magnetic.railgun.RailGunBlock;
import net.arsenalists.createenergycannons.registry.CECBlocks;
import net.arsenalists.createenergycannons.registry.CECItems;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.EntityElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBlockEntity;

public class CECPonderScenes {


    public static void energyMountSetup(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("energy_mount_setup", "Setting Up an Energy Cannon Mount");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();

        scene.overlay().showText(80)
                .text("Energy Cannon Mounts work like CBC Cannon Mounts")
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        scene.overlay().showText(60)
                .text("See Create Big Cannons 'Operating Cannons' for controls")
                .colored(PonderPalette.BLUE)
                .placeNearTarget();
        scene.idle(70);

        // Show mount + shaft
        scene.world().showSection(util.select().fromTo(2, 1, 2, 2, 2, 2), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(60)
                .text("Place the Energy Cannon Mount on a shaft")
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 2));
        scene.idle(70);

        // Show battery
        scene.world().showSection(util.select().position(1, 2, 2), Direction.EAST);
        scene.idle(20);

        scene.overlay().showText(80)
                .text("Connect a Forge Energy source - this is required!")
                .colored(PonderPalette.RED)
                .placeNearTarget()
                .pointAt(util.vector().centerOf(1, 2, 2))
                .attachKeyFrame();
        scene.idle(90);

        // Show cannon assembly (breech + chamber + barrels)
        scene.world().showSection(util.select().fromTo(2, 4, 1, 2, 4, 4), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(80)
                .text("Build a cannon 2 blocks above the mount")
                .colored(PonderPalette.GREEN)
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 4, 3));
        scene.idle(90);

        // Show assembly lever and toggle it
        BlockPos assemblyLever = new BlockPos(2, 2, 1);
        scene.world().showSection(util.select().position(assemblyLever), Direction.SOUTH);
        scene.idle(10);

        scene.overlay().showText(60)
                .text("Toggle the assembly lever to assemble the cannon")
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(assemblyLever, Direction.NORTH));
        scene.idle(30);

        scene.world().toggleRedstonePower(util.select().position(assemblyLever));
        scene.effects().indicateRedstone(assemblyLever);
        scene.idle(40);

        // Show fire lever and toggle it
        BlockPos fireLever = new BlockPos(2, 2, 3);
        scene.world().showSection(util.select().position(fireLever), Direction.NORTH);
        scene.idle(10);

        scene.overlay().showText(60)
                .text("Toggle the other lever to fire!")
                .colored(PonderPalette.RED)
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(fireLever, Direction.SOUTH))
                .attachKeyFrame();
        scene.idle(30);

        scene.world().toggleRedstonePower(util.select().position(fireLever));
        scene.effects().indicateRedstone(fireLever);
        scene.idle(20);

        // Firing effects - smoke at muzzle (z=4 is the tip)
        Vec3 muzzle = util.vector().centerOf(2, 4, 4).add(0, 0, 0.5);
        scene.effects().emitParticles(muzzle, scene.effects().simpleParticleEmitter(ParticleTypes.CAMPFIRE_COSY_SMOKE, muzzle), 0.5f, 20);
        scene.effects().emitParticles(muzzle, scene.effects().simpleParticleEmitter(ParticleTypes.FLAME, muzzle), 0.3f, 5);
        scene.idle(30);

        scene.markAsFinished();
    }

    public static void energyMountPower(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("energy_mount_power", "Powering Energy Cannons");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();

        // Show everything
        scene.world().showSection(util.select().layersFrom(1), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(80)
                .text("Energy cannons need Forge Energy (FE) to fire")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(1, 2, 2))
                .attachKeyFrame();
        scene.idle(90);

        scene.overlay().showText(80)
                .text("Rail Guns: 20,000 FE per barrel block per shot")
                .colored(PonderPalette.RED)
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 4, 3));
        scene.idle(90);

        scene.overlay().showText(80)
                .text("Coil Guns: 10,000 FE per barrel block per shot")
                .colored(PonderPalette.BLUE)
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 4, 3));
        scene.idle(90);

        scene.overlay().showText(80)
                .text("Lasers: 5 FE per tick while firing")
                .colored(PonderPalette.GREEN)
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 4, 3));
        scene.idle(90);

        scene.overlay().showText(80)
                .text("If there isn't enough energy, the cannon won't fire")
                .colored(PonderPalette.RED)
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        scene.markAsFinished();
    }


    public static void laserBasics(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("laser_basics", "Laser Cannon");
        scene.configureBasePlate(0, 0, 7);
        scene.showBasePlate();

        // Show mount + shaft + battery
        scene.world().showSection(util.select().fromTo(2, 1, 1, 3, 2, 2), Direction.DOWN);
        scene.idle(20);

        // Show assembly lever and fire lever
        scene.world().showSection(util.select().position(3, 2, 0), Direction.SOUTH);
        scene.idle(10);

        // Show laser
        scene.world().showSection(util.select().position(3, 4, 1), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(80)
                .text("Laser Cannons are single-block energy weapons")
                .colored(PonderPalette.GREEN)
                .placeNearTarget()
                .pointAt(util.vector().topOf(3, 4, 1))
                .attachKeyFrame();
        scene.idle(90);

        scene.overlay().showText(60)
                .text("No barrel needed - just the laser block and a mount!")
                .placeNearTarget()
                .pointAt(util.vector().topOf(3, 4, 1));
        scene.idle(70);

        // Show target block in beam path
        scene.world().showSection(util.select().position(3, 4, 3), Direction.DOWN);
        scene.idle(20);

        // Toggle assembly lever
        BlockPos assemblyLever = new BlockPos(3, 2, 0);
        scene.world().toggleRedstonePower(util.select().position(assemblyLever));
        scene.effects().indicateRedstone(assemblyLever);
        scene.idle(20);

        // Toggle fire lever
        BlockPos fireLever = new BlockPos(3, 2, 2);
        scene.world().toggleRedstonePower(util.select().position(fireLever));
        scene.effects().indicateRedstone(fireLever);
        scene.idle(10);

        // Animate beam - draw thick line from laser to target + particles along beam
        Vec3 beamStart = util.vector().centerOf(3, 4, 1).add(0, 0, 0.5);
        Vec3 beamHitBlock = util.vector().centerOf(3, 4, 3);
        scene.overlay().showBigLine(PonderPalette.WHITE, beamStart, beamHitBlock, 80);

        // Emit glowing particles along the beam path
        for (int i = 0; i <= 4; i++) {
            Vec3 along = beamStart.add(0, 0, i * 0.4);
            scene.effects().emitParticles(along, scene.effects().simpleParticleEmitter(ParticleTypes.END_ROD, along), 0.05f, 2);
        }
        scene.idle(20);

        scene.overlay().showText(60)
                .text("The beam breaks blocks in its path!")
                .colored(PonderPalette.RED)
                .placeNearTarget()
                .pointAt(util.vector().topOf(3, 4, 3))
                .attachKeyFrame();

        // Show block breaking
        scene.world().incrementBlockBreakingProgress(new BlockPos(3, 4, 3));
        scene.idle(10);
        scene.world().incrementBlockBreakingProgress(new BlockPos(3, 4, 3));
        scene.idle(10);
        scene.world().incrementBlockBreakingProgress(new BlockPos(3, 4, 3));
        scene.idle(10);
        scene.world().destroyBlock(new BlockPos(3, 4, 3));
        scene.idle(40);

        // Show pig platform and spawn pig
        scene.world().showSection(util.select().position(3, 3, 6), Direction.DOWN);
        scene.idle(10);

        ElementLink<EntityElement> pig = scene.world().createEntity(level -> {
            Pig p = new Pig(EntityType.PIG, level);
            p.setPos(3.5, 4, 6.5);
            p.setNoAi(true);
            return p;
        });
        scene.idle(20);

        // Beam extends to pig - thick line with particles
        Vec3 beamToPig = new Vec3(3.5, 4.5, 6.5);
        scene.overlay().showBigLine(PonderPalette.WHITE, beamStart, beamToPig, 80);

        // Particles along the full beam
        for (int i = 0; i <= 10; i++) {
            double t = i / 10.0;
            Vec3 along = beamStart.lerp(beamToPig, t);
            scene.effects().emitParticles(along, scene.effects().simpleParticleEmitter(ParticleTypes.END_ROD, along), 0.05f, 1);
        }
        scene.idle(10);

        scene.overlay().showText(80)
                .text("It also damages entities in the beam!")
                .colored(PonderPalette.RED)
                .placeNearTarget()
                .pointAt(beamToPig)
                .attachKeyFrame();

        // Damage particles at pig
        scene.effects().emitParticles(beamToPig, scene.effects().simpleParticleEmitter(ParticleTypes.DAMAGE_INDICATOR, beamToPig), 0.5f, 15);
        scene.idle(90);

        scene.overlay().showText(60)
                .text("Use CBC controls to aim the laser")
                .placeNearTarget();
        scene.idle(70);

        scene.markAsFinished();
    }


    public static void magneticCannonBasics(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("magnetic_cannon_basics", "Magnetic Cannon Basics");
        scene.configureBasePlate(0, 0, 7);
        scene.showBasePlate();

        // Show mount + battery
        scene.world().showSection(util.select().fromTo(2, 1, 1, 3, 2, 2), Direction.DOWN);
        scene.idle(20);

        // Show levers
        scene.world().showSection(util.select().position(3, 2, 0), Direction.SOUTH);
        scene.idle(10);

        scene.overlay().showText(80)
                .text("Magnetic cannons use electromagnetic acceleration instead of powder")
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        // Show breech + chamber
        scene.world().showSection(util.select().fromTo(3, 4, 0, 3, 4, 1), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(60)
                .text("Start with a CBC breech and chamber as usual")
                .placeNearTarget()
                .pointAt(util.vector().topOf(3, 4, 0));
        scene.idle(70);

        // Show railgun barrels one by one
        for (int z = 2; z <= 5; z++) {
            scene.world().showSection(util.select().position(3, 4, z), Direction.DOWN);
            scene.idle(8);
        }

        scene.overlay().showText(80)
                .text("Railgun barrel blocks act as the cannon barrel")
                .colored(PonderPalette.RED)
                .placeNearTarget()
                .pointAt(util.vector().topOf(3, 4, 3))
                .attachKeyFrame();
        scene.idle(90);

        // Animate block replacement: railgun -> coilgun
        scene.world().replaceBlocks(
                util.select().fromTo(3, 4, 2, 3, 4, 5),
                CECBlocks.STEEL_COILGUN_BARREL.get().defaultBlockState()
                        .setValue(BlockStateProperties.FACING, Direction.SOUTH),
                true);
        scene.idle(30);

        scene.overlay().showText(80)
                .text("...or use Coilgun barrel blocks instead!")
                .colored(PonderPalette.BLUE)
                .placeNearTarget()
                .pointAt(util.vector().topOf(3, 4, 3))
                .attachKeyFrame();
        scene.idle(90);

        // Switch back to railgun
        scene.world().replaceBlocks(
                util.select().fromTo(3, 4, 2, 3, 4, 5),
                CECBlocks.NETHERSTEEL_RAILGUN_BARREL.get().defaultBlockState()
                        .setValue(BlockStateProperties.FACING, Direction.SOUTH),
                true);
        scene.idle(30);

        scene.overlay().showText(80)
                .text("Use standard CBC projectiles - load through the breech")
                .colored(PonderPalette.GREEN)
                .placeNearTarget()
                .pointAt(util.vector().topOf(3, 4, 0));
        scene.idle(90);

        scene.overlay().showText(100)
                .text("IMPORTANT: Projectiles MUST have a Magnetic Sled attached!")
                .colored(PonderPalette.RED)
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(110);

        // Toggle assembly lever
        BlockPos assemblyLever = new BlockPos(3, 2, 0);
        scene.world().toggleRedstonePower(util.select().position(assemblyLever));
        scene.effects().indicateRedstone(assemblyLever);
        scene.idle(20);

        scene.overlay().showText(50)
                .text("Assemble the cannon...")
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(assemblyLever, Direction.NORTH));
        scene.idle(60);

        // Toggle fire lever
        BlockPos fireLever = new BlockPos(3, 2, 2);
        scene.world().toggleRedstonePower(util.select().position(fireLever));
        scene.effects().indicateRedstone(fireLever);
        scene.idle(10);

        // Firing effects
        Vec3 muzzle = util.vector().centerOf(3, 4, 5).add(0, 0, 0.5);
        scene.effects().emitParticles(muzzle, scene.effects().simpleParticleEmitter(ParticleTypes.CAMPFIRE_COSY_SMOKE, muzzle), 0.5f, 25);
        scene.effects().emitParticles(muzzle, scene.effects().simpleParticleEmitter(ParticleTypes.FLAME, muzzle), 0.3f, 10);
        scene.idle(15);

        scene.overlay().showText(40)
                .text("FIRE!")
                .colored(PonderPalette.RED)
                .placeNearTarget()
                .pointAt(muzzle);
        scene.idle(50);

        // Show overheating
        scene.world().modifyBlocks(
                util.select().fromTo(3, 4, 2, 3, 4, 5),
                state -> state.hasProperty(RailGunBlock.OVERHEATED) ? state.setValue(RailGunBlock.OVERHEATED, true) : state,
                false);
        scene.idle(10);

        scene.overlay().showText(100)
                .text("After firing, barrel blocks overheat for 25 seconds!")
                .colored(PonderPalette.RED)
                .placeNearTarget()
                .pointAt(util.vector().topOf(3, 4, 3))
                .attachKeyFrame();
        scene.idle(110);

        scene.overlay().showText(80)
                .text("Firing while overheated will cause a catastrophic misfire!")
                .colored(PonderPalette.RED)
                .placeNearTarget();
        scene.idle(90);

        // Cool down
        scene.world().modifyBlocks(
                util.select().fromTo(3, 4, 2, 3, 4, 5),
                state -> state.hasProperty(RailGunBlock.OVERHEATED) ? state.setValue(RailGunBlock.OVERHEATED, false) : state,
                false);
        scene.idle(20);

        scene.overlay().showText(60)
                .text("More barrel blocks = higher velocity but more energy cost")
                .colored(PonderPalette.MEDIUM)
                .placeNearTarget();
        scene.idle(70);

        scene.markAsFinished();
    }


    public static void magneticSledAssembly(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("magnetic_sled_assembly", "Magnetic Sled Assembly");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();

        scene.overlay().showText(100)
                .text("Energy cannons REQUIRE a Magnetic Sled on all projectiles!")
                .colored(PonderPalette.RED)
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(110);

        scene.overlay().showText(80)
                .text("Without a Magnetic Sled, the cannon simply won't fire")
                .colored(PonderPalette.RED)
                .placeNearTarget();
        scene.idle(90);

        // Show the shell
        BlockPos shellPos = new BlockPos(2, 1, 2);
        scene.world().showSection(util.select().position(shellPos), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(60)
                .text("Start with any CBC projectile (shells, AP shells, etc)")
                .placeNearTarget()
                .pointAt(util.vector().topOf(shellPos));
        scene.idle(70);

        // Show right-click with magnetic sled
        scene.overlay().showControls(
                        util.vector().blockSurface(shellPos, Direction.UP),
                        Pointing.DOWN, 80)
                .rightClick()
                .withItem(new ItemStack(CECItems.MAGNETIC_SLED.get()));
        scene.idle(20);

        scene.overlay().showText(80)
                .text("Right-click the shell with a Magnetic Sled")
                .colored(PonderPalette.GREEN)
                .placeNearTarget()
                .pointAt(util.vector().topOf(shellPos))
                .attachKeyFrame();
        scene.idle(90);

        // Set sled NBT to show the sled model on the shell
        scene.world().modifyBlockEntityNBT(
                util.select().position(shellPos),
                FuzedBlockEntity.class,
                tag -> tag.putBoolean("Sled", true));
        scene.effects().indicateSuccess(shellPos);
        scene.idle(20);

        scene.overlay().showText(80)
                .text("The Magnetic Sled is now attached - ready for energy cannons!")
                .colored(PonderPalette.GREEN)
                .placeNearTarget()
                .pointAt(util.vector().topOf(shellPos));
        scene.idle(90);

        scene.overlay().showText(80)
                .text("Load the sledded projectile into the breech like normal")
                .placeNearTarget();
        scene.idle(90);

        scene.markAsFinished();
    }

    public static void railgunVsCoilgun(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("railgun_vs_coilgun", "Railgun vs Coilgun");
        scene.configureBasePlate(0, 0, 7);
        scene.showBasePlate();

        // Show everything
        scene.world().showSection(util.select().layersFrom(1), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(60)
                .text("RAILGUN")
                .colored(PonderPalette.RED)
                .placeNearTarget()
                .pointAt(util.vector().topOf(3, 4, 3))
                .attachKeyFrame();
        scene.idle(70);

        scene.overlay().showText(80)
                .text("20,000 FE per barrel block per shot")
                .colored(PonderPalette.RED)
                .placeNearTarget()
                .pointAt(util.vector().topOf(3, 4, 3));
        scene.idle(90);

        // Show charging animation
        scene.world().modifyBlocks(
                util.select().fromTo(3, 4, 2, 3, 4, 5),
                state -> state.hasProperty(RailGunBlock.CHARGING) ? state.setValue(RailGunBlock.CHARGING, true) : state,
                false);
        scene.idle(10);

        scene.overlay().showText(80)
                .text("1-second charging phase before firing")
                .colored(PonderPalette.SLOW)
                .placeNearTarget()
                .pointAt(util.vector().topOf(3, 4, 3));
        scene.idle(90);

        // Fire
        scene.world().modifyBlocks(
                util.select().fromTo(3, 4, 2, 3, 4, 5),
                state -> state.hasProperty(RailGunBlock.CHARGING) ? state.setValue(RailGunBlock.CHARGING, false) : state,
                false);

        Vec3 muzzle = util.vector().centerOf(3, 4, 5).add(0, 0, 0.5);
        scene.effects().emitParticles(muzzle, scene.effects().simpleParticleEmitter(ParticleTypes.CAMPFIRE_COSY_SMOKE, muzzle), 0.5f, 20);
        scene.effects().emitParticles(muzzle, scene.effects().simpleParticleEmitter(ParticleTypes.FLAME, muzzle), 0.3f, 8);
        scene.idle(20);

        // Overheat railgun
        scene.world().modifyBlocks(
                util.select().fromTo(3, 4, 2, 3, 4, 5),
                state -> state.hasProperty(RailGunBlock.OVERHEATED) ? state.setValue(RailGunBlock.OVERHEATED, true) : state,
                false);

        scene.overlay().showText(80)
                .text("Higher velocity but expensive - and overheats!")
                .colored(PonderPalette.RED)
                .placeNearTarget()
                .pointAt(util.vector().topOf(3, 4, 3));
        scene.idle(90);

        // Cool down before switching
        scene.world().modifyBlocks(
                util.select().fromTo(3, 4, 2, 3, 4, 5),
                state -> state.hasProperty(RailGunBlock.OVERHEATED) ? state.setValue(RailGunBlock.OVERHEATED, false) : state,
                false);
        scene.idle(10);

        // Switch to coilgun
        scene.world().replaceBlocks(
                util.select().fromTo(3, 4, 2, 3, 4, 5),
                CECBlocks.STEEL_COILGUN_BARREL.get().defaultBlockState()
                        .setValue(BlockStateProperties.FACING, Direction.SOUTH),
                true);
        scene.idle(30);

        scene.overlay().showText(60)
                .text("COILGUN")
                .colored(PonderPalette.BLUE)
                .placeNearTarget()
                .pointAt(util.vector().topOf(3, 4, 3))
                .attachKeyFrame();
        scene.idle(70);

        scene.overlay().showText(80)
                .text("10,000 FE per barrel block per shot - half the cost!")
                .colored(PonderPalette.BLUE)
                .placeNearTarget()
                .pointAt(util.vector().topOf(3, 4, 3));
        scene.idle(90);

        scene.overlay().showText(80)
                .text("Fires instantly - no charging phase!")
                .colored(PonderPalette.GREEN)
                .placeNearTarget();
        scene.idle(90);

        // Fire coilgun - with sonic boom effects
        scene.effects().emitParticles(muzzle, scene.effects().simpleParticleEmitter(ParticleTypes.CAMPFIRE_COSY_SMOKE, muzzle), 0.5f, 20);
        scene.effects().emitParticles(muzzle, scene.effects().simpleParticleEmitter(ParticleTypes.FLAME, muzzle), 0.3f, 8);

        // Sonic boom at each barrel
        for (int z = 2; z <= 5; z++) {
            Vec3 barrelCenter = util.vector().centerOf(3, 4, z);
            scene.effects().emitParticles(barrelCenter, scene.effects().simpleParticleEmitter(ParticleTypes.SONIC_BOOM, barrelCenter), 0.2f, 3);
        }
        scene.idle(30);

        // Overheat coilgun
        scene.world().modifyBlocks(
                util.select().fromTo(3, 4, 2, 3, 4, 5),
                state -> state.hasProperty(CoilGunBlock.OVERHEATED) ? state.setValue(CoilGunBlock.OVERHEATED, true) : state,
                false);
        scene.idle(10);

        scene.overlay().showText(80)
                .text("Lower velocity - but also overheats for 25 seconds!")
                .colored(PonderPalette.BLUE)
                .placeNearTarget()
                .pointAt(util.vector().topOf(3, 4, 3));
        scene.idle(90);

        scene.overlay().showText(100)
                .text("BOTH types overheat - plan your shots carefully!")
                .colored(PonderPalette.RED)
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(110);

        scene.markAsFinished();
    }
}
