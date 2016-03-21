package com.clearcapital.oss.cassandra_dropwizard.cli;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clearcapital.oss.cassandra.annotation_processors.CassandraTableProcessor;
import com.clearcapital.oss.cassandra.configuration.AutoSchemaConfiguration;
import com.clearcapital.oss.cassandra.configuration.WithMultiRingConfiguration;
import com.clearcapital.oss.cassandra.multiring.MultiRingClientManager;
import com.clearcapital.oss.dropwizard.helpers.ConfiguredCommandLineTool;
import com.clearcapital.oss.executors.ImmediateCommandExecutor;
import com.clearcapital.oss.java.AssertHelpers;
import com.google.common.collect.ImmutableSet;

import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class AutoSchemaCommand<T extends Configuration> extends ConfiguredCommandLineTool<T> {

    private static Logger log = LoggerFactory.getLogger(AutoSchemaCommand.class);
    private final Class<T> configurationClass;

    public AutoSchemaCommand(Class<T> configurationClass) {
        super("auto-schema", "Check the configured keyspaces to ensure their schemas are correct.");
        this.configurationClass = configurationClass;
    }

    @Override
    protected Class<T> getConfigurationClass() {
        return configurationClass;
    }

    @Override
    public void configure(final Subparser subparser) {
        super.configure(subparser);
        subparser.addArgument("-d", "--dry-run").action(Arguments.storeTrue()).dest("dry-run")
                .help("Generate CQL, but do not execute it. Otherwise, generate CQL and execute it.");
        subparser.addArgument("-k", "--kill-schemas-first").action(Arguments.storeTrue()).dest("kill-schemas-first")
                .help("Completely kill pre-existing schema first.");
        subparser
                .addArgument("--drop-columns")
                .type(String.class)
                .nargs("+")
                .help("Columns to drop, {table}.{column} syntax. Will only drop columns which are listed here, AND which are superfluous according to the code.");
        subparser
                .addArgument("--drop-tables")
                .type(String.class)
                .nargs("+")
                .help("Tables to drop. Will only drop tables which are listed here, AND which are superfluous according to the code.");
    }

    @Override
    protected int execute(Bootstrap<T> bootstrap, Namespace namespace, T configuration) throws Exception {
        AssertHelpers.notNull(configuration, "Configuration");
        AssertHelpers.isTrue(configuration instanceof WithMultiRingConfiguration,
                "configuration instanceof WithMultiRingConfiguration");
        log.debug("==== Running auto-schema ====");

        // Convert parameters to appropriate types...
        WithMultiRingConfiguration config = (WithMultiRingConfiguration) configuration;
        Collection<String> dropColumns = namespace.get("drop_columns");
        Collection<String> dropTables = namespace.get("drop_tables");

        AutoSchemaConfiguration.Builder configBuilder = AutoSchemaConfiguration.builder();
        configBuilder.setDryRun(namespace.getBoolean("dry-run"));
        configBuilder.setDropColumns(ImmutableSet.<String> copyOf(dropColumns));
        configBuilder.setDropTables(ImmutableSet.<String> copyOf(dropTables));

        // Hand off to oss-cassandra-helpers
        CassandraTableProcessor.schemaComparator(ImmediateCommandExecutor.getInstance(),
                new MultiRingClientManager(config.getMultiRingConfiguration()), configBuilder.build()).compare();

        return 0;
    }
}