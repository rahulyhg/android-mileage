package com.evancharlton.mileage.provider;

import java.util.ArrayList;
import java.util.HashMap;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.CachedValue;

public final class Statistics {
	public enum Vehicle {
		FARTHEST_NORTH, FARTHEST_SOUTH, FARTHEST_EAST, FARTHEST_WEST
	}

	public static final HashMap<String, Statistic> STRINGS = new HashMap<String, Statistic>();
	public static final ArrayList<Statistic> STATISTICS = new ArrayList<Statistic>();
	public static final ArrayList<StatisticsGroup> GROUPS = new ArrayList<StatisticsGroup>();

	public static final Statistic AVG_ECONOMY = new Statistic(R.string.stat_avg_economy, new CachedValue("average_economy"));
	public static final Statistic MIN_ECONOMY = new Statistic(R.string.stat_min_economy, new CachedValue("minimum_economy"));
	public static final Statistic MAX_ECONOMY = new Statistic(R.string.stat_max_economy, new CachedValue("maximum_economy"));
	public static final StatisticsGroup ECONOMIES = new StatisticsGroup(R.string.stat_fuel_economy, AVG_ECONOMY, MIN_ECONOMY, MAX_ECONOMY);

	public static final Statistic AVG_DISTANCE = new Statistic(R.string.stat_avg_distance, new CachedValue("average_distance"));
	public static final Statistic MIN_DISTANCE = new Statistic(R.string.stat_min_distance, new CachedValue("minimum_distance"));
	public static final Statistic MAX_DISTANCE = new Statistic(R.string.stat_max_distance, new CachedValue("maximum_distance"));
	public static final StatisticsGroup DISTANCES = new StatisticsGroup(R.string.stat_distance_between_fillups, AVG_DISTANCE, MIN_DISTANCE,
			MAX_DISTANCE);

	public static final Statistic AVG_COST = new Statistic(R.string.stat_avg_cost, new CachedValue("average_cost"));
	public static final Statistic MIN_COST = new Statistic(R.string.stat_min_cost, new CachedValue("minimum_cost"));
	public static final Statistic MAX_COST = new Statistic(R.string.stat_max_cost, new CachedValue("maximum_cost"));
	public static final Statistic TOTAL_COST = new Statistic(R.string.stat_total_cost, new CachedValue("total_cost"));
	public static final Statistic MONTHLY_COST = new Statistic(R.string.stat_month_cost, new CachedValue("monthly_cost"));
	public static final Statistic YEARLY_COST = new Statistic(R.string.stat_year_cost, new CachedValue("yearly_cost"));
	public static final StatisticsGroup COSTS = new StatisticsGroup(R.string.stat_fillup_cost, AVG_COST, MIN_COST, MAX_COST, TOTAL_COST,
			MONTHLY_COST, YEARLY_COST);

	public static final Statistic AVG_COST_PER_DISTANCE = new Statistic(R.string.stat_avg_cost_per_distance, new CachedValue(
			"average_cost_per_distance"));
	public static final Statistic MIN_COST_PER_DISTANCE = new Statistic(R.string.stat_min_cost_per_distance, new CachedValue(
			"minimum_cost_per_distance"));
	public static final Statistic MAX_COST_PER_DISTANCE = new Statistic(R.string.stat_max_cost_per_distance, new CachedValue(
			"maximum_cost_per_distance"));
	public static final StatisticsGroup COSTS_PER_DISTANCE = new StatisticsGroup(R.string.stat_cost_per_distance, AVG_COST_PER_DISTANCE,
			MIN_COST_PER_DISTANCE, MAX_COST_PER_DISTANCE);

	public static final Statistic AVG_PRICE = new Statistic(R.string.stat_avg_price, new CachedValue("average_price"));
	public static final Statistic MIN_PRICE = new Statistic(R.string.stat_min_price, new CachedValue("minimum_price"));
	public static final Statistic MAX_PRICE = new Statistic(R.string.stat_max_price, new CachedValue("maximum_price"));
	public static final StatisticsGroup PRICES = new StatisticsGroup(R.string.stat_price, AVG_PRICE, MIN_PRICE, MAX_PRICE);

	public static final Statistic MIN_FUEL = new Statistic(R.string.stat_min_fuel, new CachedValue("minimum_fuel"));
	public static final Statistic MAX_FUEL = new Statistic(R.string.stat_max_fuel, new CachedValue("maximum_fuel"));
	public static final Statistic AVG_FUEL = new Statistic(R.string.stat_avg_fuel, new CachedValue("average_fuel"));
	public static final Statistic TOTAL_FUEL = new Statistic(R.string.stat_total_fuel, new CachedValue("total_fuel"));
	public static final Statistic FUEL_PER_YEAR = new Statistic(R.string.stat_fuel_per_year, new CachedValue("fuel_per_year"));
	public static final StatisticsGroup VOLUMES = new StatisticsGroup(R.string.stat_fuel, MIN_FUEL, MAX_FUEL, AVG_FUEL, TOTAL_FUEL, FUEL_PER_YEAR);

	public static final Statistic NORTH = new Statistic(R.string.stat_north, new CachedValue("north"));
	public static final Statistic SOUTH = new Statistic(R.string.stat_south, new CachedValue("south"));
	public static final Statistic EAST = new Statistic(R.string.stat_east, new CachedValue("east"));
	public static final Statistic WEST = new Statistic(R.string.stat_west, new CachedValue("west"));
	public static final StatisticsGroup LOCATION = new StatisticsGroup(R.string.stat_location, NORTH, SOUTH, EAST, WEST);

	public static class Statistic {
		private final int mLabel;
		private final CachedValue mValue;

		public Statistic(int label, CachedValue value) {
			STATISTICS.add(this);
			STRINGS.put(value.getKey(), this);
			mLabel = label;
			mValue = value;
		}

		public int getLabel() {
			return mLabel;
		}

		public void setValue(double value) {
			mValue.setValue(value);
		}

		public double getValue() {
			return mValue.getValue();
		}

		public String getKey() {
			return mValue.getKey();
		}

		public void setGroup(long group) {
			mValue.setGroup(group);
		}

		public void setOrder(long order) {
			mValue.setOrder(order);
		}

		@Override
		public String toString() {
			return mValue.getKey() + " - " + mValue.getValue();
		}
	}

	public static class StatisticsGroup {
		private final ArrayList<Statistic> mStatistics = new ArrayList<Statistic>();
		private final int mLabel;

		public StatisticsGroup(int label, Statistic... statistics) {
			GROUPS.add(this);
			final int length = statistics.length;
			int group = GROUPS.size();
			for (int i = 0; i < length; i++) {
				statistics[i].setGroup(group);
				statistics[i].setOrder(i + 1);
				mStatistics.add(statistics[i]);
			}
			mLabel = label;
		}

		public int getLabel() {
			return mLabel;
		}
	}
}
