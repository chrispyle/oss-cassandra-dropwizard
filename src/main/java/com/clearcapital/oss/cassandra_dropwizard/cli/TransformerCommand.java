package com.clearcapital.oss.cassandra_dropwizard.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clearcapital.oss.cassandra.annotation_processors.TransformerProcessor;
import com.clearcapital.oss.cassandra.configuration.WithMultiRingConfiguration;
import com.clearcapital.oss.dropwizard.helpers.ConfiguredCommandLineTool;

import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class TransformerCommand<T extends Configuration> extends ConfiguredCommandLineTool<T> {

    private static Logger log = LoggerFactory.getLogger(TransformerCommand.class);

    private static final String NONVNODE_END_TOKEN = "nonvnode-endToken";
    private static final String NONVNODE_START_TOKEN = "nonvnode-startToken";
    private static final String VNODE_START = "vnode-start";
    private static final String VNODE_DC = "vnode-dc";
    private static final String VNODE_HOST = "vnode-host";
    private static final String TRANSFORMER = "transformer";
    private static final String USE_VNODES = "vnodes";
    private static final String LIST = "list";
    private final Class<T> configurationClass;

    public TransformerCommand(Class<T> configurationClass) {
        super(TRANSFORMER, "Transform a table.");
        this.configurationClass = configurationClass;
    }

    @Override
    protected Class<T> getConfigurationClass() {
        return configurationClass;
    }

    @Override
    public void configure(final Subparser subparser) {
        super.configure(subparser);

        subparser.addArgument("-l", "--list").action(Arguments.storeTrue()).dest(LIST)
                .help("List transformers. Does not perform any modifications");

        subparser.addArgument("-t", "--transformer").type(String.class).dest(TRANSFORMER)
                .help("Transformer plugin to run");

        subparser.addArgument("-v", "--vnodes-enabled").action(Arguments.storeTrue()).dest(USE_VNODES)
                .help("Use vnodes to decide on token ranges");

        subparser.addArgument("-vh", "--vnode-host").type(String.class).dest(VNODE_HOST)
                .help("[vnodes-enabled mode]: Host name to scan for vnodes");
        subparser.addArgument("-vd", "--vnode-dc").type(String.class).dest(VNODE_DC)
                .help("[vnodes-enabled mode]: DC to scan for vnodes");
        subparser.addArgument("-vs", "--vnode-start").type(Integer.class).dest(VNODE_START).setDefault(0L)
                .help("[vnodes-enabled mode]: Vnode to start with");

        subparser.addArgument("-ns", "--non-vnode-startToken").dest(NONVNODE_START_TOKEN).type(Long.class)
                .setDefault(Long.MIN_VALUE).help("[vnodes-disabled mode]: First token to consider");
        subparser.addArgument("-ne", "--non-vnode-endToken").dest(NONVNODE_END_TOKEN).type(Long.class)
                .setDefault(Long.MAX_VALUE).help("[vnodes-disabled mode]: Last token to consider");
    }

    @Override
    protected int execute(Bootstrap<T> bootstrap, Namespace namespace, T configuration) throws Exception {
        log.debug("Passing arguments to TransformerProcessor.");
        WithMultiRingConfiguration config = (WithMultiRingConfiguration) (configuration);
        // @formatter:off
        TransformerProcessor.builder()
            .setListTransformers(namespace.getBoolean(LIST))
            .setTransformer(namespace.getString(TRANSFORMER))
            .setVnodesEnabled(namespace.getBoolean(USE_VNODES))
            .setVnodeDC(namespace.getString(VNODE_DC))
            .setVnodeHost(namespace.getString(VNODE_HOST))
            .setVnodeStart(namespace.getInt(VNODE_START))
            .setNonVnodeStartToken(namespace.getLong(NONVNODE_START_TOKEN))
            .setNonVnodeEndToken(namespace.getLong(NONVNODE_END_TOKEN))
            .setConfiguration(config)
            .execute();
        // @formatter:on

        return 0;
    }

}
