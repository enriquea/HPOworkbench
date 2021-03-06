package org.monarchinitiative.hpoworkbench.cmd;


import org.apache.log4j.Logger;
import org.monarchinitiative.hpoworkbench.exception.HPOException;
import org.monarchinitiative.hpoworkbench.io.HPOAnnotationParser;
import org.monarchinitiative.hpoworkbench.io.HpoOntologyParser;
import org.monarchinitiative.phenol.formats.hpo.HpoDiseaseAnnotation;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.formats.hpo.HpoTerm;
import org.monarchinitiative.phenol.graph.data.Edge;
import org.monarchinitiative.phenol.ontology.data.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class drives the HPO term "cross-correlation" analysis.
 */
public class Hpo2HpoCommand extends HPOCommand {
    private static Logger LOGGER = Logger.getLogger(Hpo2HpoCommand.class.getName());
    private final String hpOboPath;

    private final String annotationPath;

    private final TermId termId;
    /** All of the ancestor terms of {@link #termId}. */
    private Set<TermId> descendents=null;
    /** Annotations of all of the diseases in the HPO corpus. */
    private List<HpoDiseaseAnnotation> annotlist=null;

    private HpoOntology ontology=null;


    public Hpo2HpoCommand(String hpoPath, String annotPath, String hpoTermId) {
        this.hpOboPath = hpoPath;
        this.annotationPath = annotPath;

        if (hpoTermId.startsWith("HP:")) {
            hpoTermId = hpoTermId.substring(3);
        }
        TermPrefix HP_PREFIX = new ImmutableTermPrefix("HP");
        termId = new ImmutableTermId(HP_PREFIX, hpoTermId);
    }



        /**
         * Function for the execution of the command.
         *
         * @throws HPOException on problems executing the command.
         */
    @Override public  void run() {
        inputHpoData();
    }

    /** input the hp.obo and the annotations. */
    private void inputHpoData() {
        try {
            HpoOntologyParser oparser = new HpoOntologyParser(hpOboPath);
            this.ontology = oparser.getOntology();
            HPOAnnotationParser aparser = new HPOAnnotationParser(annotationPath);
            this.annotlist = aparser.getAnnotations();
            this.descendents = getDescendents(ontology, termId);
        } catch (HPOException e) {
            LOGGER.error(String.format("Could not input ontology: %s",e.getMessage()));
            System.exit(1);
        }
    }



    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        return map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    private void outputCounts(HashMap<TermId,Double> hm, Ontology ontology) {
        Map mp2 = sortByValue(hm);
        for (Object t: mp2.keySet()) {
            TermId tid = (TermId) t;
            double count = (double)mp2.get(t);
            String name =  ((HpoTerm)ontology.getTermMap().get(tid)).getName();
            System.out.println(name + " [" +tid.getIdWithPrefix() + "]: " + count);
        }
    }



    /** Get the immediate children of a Term. Do not include the original term in the returned set. */
    private  Set<TermId> getTermChildren(Ontology ontology, TermId id) {
        Set<TermId> kids = new HashSet<>();

        Iterator it =  ontology.getGraph().inEdgeIterator(id);
        while (it.hasNext()) {
            Edge<TermId> edge = (Edge<TermId>) it.next();
            TermId sourceId=edge.getSource();
            kids.add(sourceId);
        }
        return kids;
    }

    public Set<TermId> getDescendents(Ontology ontology, TermId parent) {
        Set<TermId> descset = new HashSet<>();
        Stack<TermId> stack = new Stack<>();
        stack.push(parent);
        while (! stack.empty() ) {
            TermId tid = stack.pop();
            descset.add(tid);
            Set<TermId> directChildrenSet = getTermChildren(ontology,tid);
            directChildrenSet.stream().forEach(t -> stack.push(t));
        }
        return descset;
    }


    @Override public String getName() { return "hpo2hpo"; };
}
