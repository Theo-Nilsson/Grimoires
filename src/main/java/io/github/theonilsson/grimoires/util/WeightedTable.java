package io.github.theonilsson.grimoires.util;

import java.util.*;

public final class WeightedTable<T> {

	private final TreeMap<Integer, WeightedPool<T>> tiers = new TreeMap<>();

	public WeightedTable<T> add(WeightedPool<T> pool, int cost) {
		tiers.put(cost, pool);
		return this;
	}

	public List<T> roll(int maxCount, int budget, float spread, Random rng) {
		if (tiers.isEmpty()) return List.of();
		spread = Math.max(1f, spread);

		List<T> out = new ArrayList<>();
		int cheapest = tiers.firstKey();

		while (maxCount > 0 && budget >= cheapest) {
			double avg = (double) budget / maxCount;

			double factor = Math.exp((rng.nextDouble() * 2 - 1) * Math.log(spread));
			int target = (int) Math.round(avg * factor);

			Map.Entry<Integer, WeightedPool<T>> tier = tiers.floorEntry(target);
			if (tier == null) tier = tiers.firstEntry();

			out.add(tier.getValue().pick(rng));
			budget -= tier.getKey();
			maxCount--;
		}
		return out;
	}
}
