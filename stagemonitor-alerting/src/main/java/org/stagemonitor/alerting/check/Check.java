package org.stagemonitor.alerting.check;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import org.stagemonitor.core.metrics.metrics2.MetricName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

/**
 * A {@link Check} is a named collection of {@link Threshold}s with the same {@link MetricCategory} and target.
 * <p/>
 * Example: We have a check group named 'Search response time' with two checks both have the target
 * <code>requests.search.time</code>. The first check is that the 75th
 * percentile shall be < 4sec and the second one says the 99th percentile shall be < 10 sec.
 */
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class Check {

	private String id = UUID.randomUUID().toString();
	private String name;
	private MetricName target;
	private int alertAfterXFailures = 1;
	private Map<CheckResult.Status, List<Threshold>> thresholds = new LinkedHashMap<CheckResult.Status, List<Threshold>>(){{
		put(CheckResult.Status.CRITICAL, new LinkedList<Threshold>());
		put(CheckResult.Status.ERROR, new LinkedList<Threshold>());
		put(CheckResult.Status.WARN, new LinkedList<Threshold>());
	}};
	private String application;
	private boolean active = true;

	/**
	 * Performs threshold checks for the whole check group
	 *
	 * @param currentValuesByMetric the values of the target
	 * @param actualTarget the actual target that matched the {@link #target} pattern
	 * @return a list of check results (results with OK statuses are omitted)
	 */
	public List<CheckResult> check(MetricName actualTarget, Map<String, Number> currentValuesByMetric) {
		SortedMap<CheckResult.Status, List<Threshold>> sortedThresholds = new TreeMap<CheckResult.Status, List<Threshold>>(thresholds);
		for (Map.Entry<CheckResult.Status, List<Threshold>> entry : sortedThresholds.entrySet()) {
			List<CheckResult> results = checkThresholds(entry.getValue(), entry.getKey(), actualTarget, currentValuesByMetric);
			if (!results.isEmpty()) {
				return results;
			}
		}
		return Collections.emptyList();
	}

	private List<CheckResult> checkThresholds(List<Threshold> thresholds, CheckResult.Status severity,
											  MetricName actualTarget, Map<String, Number> currentValuesByMetric) {
		List<CheckResult> results = new ArrayList<CheckResult>(thresholds.size());
		for (Threshold threshold : thresholds) {
			CheckResult result = threshold.check(severity, actualTarget, currentValuesByMetric);
			if (result.getStatus() != CheckResult.Status.OK) {
				results.add(result);
			}
		}
		return results;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public MetricName getTarget() {
		return target;
	}

	public void setTarget(MetricName target) {
		this.target = target;
	}

	public int getAlertAfterXFailures() {
		return alertAfterXFailures;
	}

	public void setAlertAfterXFailures(int alertAfterXFailures) {
		this.alertAfterXFailures = alertAfterXFailures;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public List<Threshold> getWarn() {
		return thresholds.get(CheckResult.Status.WARN);
	}

	public List<Threshold> getError() {
		return thresholds.get(CheckResult.Status.ERROR);
	}

	public List<Threshold> getCritical() {
		return thresholds.get(CheckResult.Status.CRITICAL);
	}

	public List<Threshold> getThresholds(CheckResult.Status severity) {
		return thresholds.get(severity);
	}

}
