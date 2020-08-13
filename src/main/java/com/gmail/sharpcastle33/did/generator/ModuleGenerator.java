package com.gmail.sharpcastle33.did.generator;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import org.bukkit.Bukkit;

public class ModuleGenerator {

	public static void read(CaveGenContext ctx, String cave, Vector3 start, Vector3 dir, int caveRadius, List<Centroid> centroids) {
		Bukkit.getLogger().log(Level.INFO, "Beginning module generation... " + cave.length() + " modules.");
		Bukkit.getLogger().log(Level.INFO, "Cave string: " + cave);

		Map<Character, Room> rooms = ctx.style.getRooms().stream()
				.collect(Collectors.groupingBy(Room::getSymbol, Collectors.reducing(null, (a, b) -> a == null ? b : a)));

		int startIndex = centroids.size();

		Vector3 location = start;
		for (int i = 0; i < cave.length(); i++) {
			Room room = rooms.get(cave.charAt(i));
			Object[] userData = room.createUserData(ctx, location, dir, caveRadius);
			room.addCentroids(ctx, location, dir, caveRadius, userData, centroids);
			dir = room.adjustDirection(ctx, dir, userData);
			location = room.adjustLocation(ctx, location, dir, caveRadius, userData);
		}

		for (int i = startIndex; i < centroids.size(); i++) {
			Centroid centroid = centroids.get(i);
			deleteSphere(ctx, centroid.pos, centroid.size);
		}
	}

	private static void deleteSphere(CaveGenContext ctx, Vector3 loc, int r) {
		double x = loc.getX();
		double y = loc.getY();
		double z = loc.getZ();

		for(int tx=-r; tx< r+1; tx++){
			for(int ty=-r; ty< r+1; ty++){
				for(int tz=-r; tz< r+1; tz++){
					if(tx * tx  +  ty * ty  +  tz * tz <= (r-2) * (r-2)){
						//delete(tx+x, ty+y, tz+z);
						if(((tx == 0 && ty == 0) || (tx == 0 && tz == 0) || (ty == 0 && tz == 0)) && (Math.abs(tx+ty+tz) == r-2)) {
							continue;
						}
						if(ty+y > 0) {
							ctx.setBlock(BlockVector3.at(tx + x, ty + y, tz + z), ctx.style.getAirBlock());
						}
					}
				}
			}
		}
	}

	public static Vector3 vary(CaveGenContext ctx, Vector3 loc) {
		int x = ctx.rand.nextInt(2)-1;
		int y = ctx.rand.nextInt(2)-1;
		int z = ctx.rand.nextInt(2)-1;
		return loc.add(x,y,z);
	}

	public static int generateOreCluster(CaveGenContext ctx, BlockVector3 loc, int radius, List<BlockStateHolder<?>> oldBlocks, BlockStateHolder<?> ore) throws MaxChangedBlocksException {
		int x = loc.getBlockX();
		int y = loc.getBlockY();
		int z = loc.getBlockZ();
		int count = 0;

		for(int tx = -radius; tx< radius +1; tx++){
			for(int ty = -radius; ty< radius +1; ty++){
				for(int tz = -radius; tz< radius +1; tz++){
					if(tx * tx  +  ty * ty  +  tz * tz <= (radius - 2) * (radius - 2)) {
						if(ty+y > 0) {
							BlockVector3 pos = BlockVector3.at(tx+x, ty+y, tz+z);

							BlockState block = ctx.getBlock(pos);
							boolean canPlaceOre;
							if (oldBlocks == null) {
								canPlaceOre = !ctx.style.isTransparentBlock(block);
							} else {
								canPlaceOre = oldBlocks.stream().anyMatch(oldBlock -> oldBlock.equalsFuzzy(block));
							}
							if(canPlaceOre) {
								if(((tx == 0 && ty == 0) || (tx == 0 && tz == 0) || (ty == 0 && tz == 0)) && (Math.abs(tx+ty+tz) == radius - 2)) {
									if(ctx.rand.nextBoolean())
										continue;
								}
								ctx.setBlock(pos, ore);
								count++;
							}

						}
					}
				}
			}
		}

		return count;
	}


}
