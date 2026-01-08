/**
 * Provides versioned, one-shot migration utilities for HavenBags configuration
 * and data. Converters run during plugin startup when an older
 * {@code config-version} is detected and are not intended for direct use.
 *
 * Responsibilities include:
 * <ul>
 *   <li>Renaming bag directories from player names to UUIDs (upgrade to
 *       {@code config-version} 2).</li>
 *   <li>Migrating per-bag content from legacy JSON files to YAML with
 *       structured metadata (upgrade to {@code config-version} 4).</li>
 *   <li>Renaming token settings from {@code skin-token.*} to
 *       {@code token.skin.*} (upgrade to {@code config-version} 5).</li>
 * </ul>
 *
 * Conventions:
 * <ul>
 *   <li>Each converter is guarded by a {@code config-version} check and aims to
 *       be idempotent.</li>
 *   <li>Operations perform filesystem I/O and log progress, warnings, and
 *       errors.</li>
 *   <li>Intended to run single-threaded during plugin initialization.</li>
 * </ul>
 *
 * See also:
 * {@link valorless.havenbags.configconversion.BagConversion},
 * {@link valorless.havenbags.configconversion.DataConversion},
 * {@link valorless.havenbags.configconversion.TokenConfigConversion}
 */
package valorless.havenbags.configconversion;
