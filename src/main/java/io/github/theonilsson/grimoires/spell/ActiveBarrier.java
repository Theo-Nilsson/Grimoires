package io.github.theonilsson.grimoires.spell;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;
import java.util.UUID;

public class ActiveBarrier {
	public final UUID ownerId;
	public final Vec3d center;
	public final Vec3d normal; // unit length
	public final Vec3d axisU;  // unit, in the disc plane
	public final Vec3d axisV;  // unit, in the disc plane
	public final float radius;
	public final int color;
	public final CastContext ctx; // for element hooks

	public int ticksLeft;
	public float durability;

	public ActiveBarrier(UUID ownerId, Vec3d center, Vec3d normal, float radius,
						 int durationTicks, float durability, int color,
						 CastContext ctx) {
		this.ownerId = ownerId;
		this.center = center;
		this.normal = normal.normalize();
		this.radius = radius;
		this.ticksLeft = durationTicks;
		this.durability = durability;
		this.color = color;
		this.ctx = ctx;

		Vec3d up = Math.abs(this.normal.y) > 0.99 ? new Vec3d(1, 0, 0) : new Vec3d(0, 1, 0);
		this.axisU = this.normal.crossProduct(up).normalize();
		this.axisV = this.normal.crossProduct(this.axisU).normalize();
	}

	public Box bounds() {
		return new Box(center, center).expand(radius + 3.0);
	}

	/** Signed distance from the disc's plane (positive = in front). */
	public double side(Vec3d p) {
		return p.subtract(center).dotProduct(normal);
	}

	/** If the segment a->b crosses the disc, returns the crossing point. Works for fast projectiles. */
	public Optional<Vec3d> crossing(Vec3d a, Vec3d b) {
		double da = side(a);
		double db = side(b);
		if (da * db > 0 || da == db) return Optional.empty();
		double t = da / (da - db);
		Vec3d p = a.add(b.subtract(a).multiply(t));
		return p.squaredDistanceTo(center) <= (double) radius * radius ? Optional.of(p) : Optional.empty();
	}

	/** Distance from the disc's axis (in-plane distance) for a point. */
	public double planarDistance(Vec3d p) {
		Vec3d rel = p.subtract(center);
		Vec3d inPlane = rel.subtract(normal.multiply(rel.dotProduct(normal)));
		return inPlane.length();
	}
}
