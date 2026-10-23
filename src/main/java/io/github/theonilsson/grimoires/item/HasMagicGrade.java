package io.github.theonilsson.grimoires.item;

public interface HasMagicGrade {

	Grade getMagicGrade();

	enum Grade {
		LEGENDARY(5),
		AGE_OLD(4),
		FIRST_CLASS(3),
		SANCTIONED(2),
		WAYFARER(1),
		HEARTHWORN(0);

		private final int rank;

		Grade(int rank) {
			this.rank = rank;
		}

		public int getRank() { return rank; }

		public String translationKey() {
			return "rarity.grimoires" + name().toLowerCase();
		}
	}
}
