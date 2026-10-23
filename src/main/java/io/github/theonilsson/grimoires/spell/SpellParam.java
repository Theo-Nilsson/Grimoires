package io.github.theonilsson.grimoires.spell;

public record SpellParam(String id, float min, float max, float defaultValue, float costWeight) {

	public float clamp(float v) {
		return Math.max(min, Math.min(max, v));
	}

	/** 0..1 position inside the allowed range. */
	public float normalized(float v) {
		return max == min ? 0f : (clamp(v) - min) / (max - min);
	}
}
