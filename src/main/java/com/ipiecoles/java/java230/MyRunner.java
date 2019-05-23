package com.ipiecoles.java.java230;

import com.ipiecoles.java.java230.exceptions.BatchException;
import com.ipiecoles.java.java230.model.Commercial;
import com.ipiecoles.java.java230.model.Employe;
import com.ipiecoles.java.java230.model.Manager;
import com.ipiecoles.java.java230.model.Technicien;
import com.ipiecoles.java.java230.repository.EmployeRepository;
import com.ipiecoles.java.java230.repository.ManagerRepository;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cglib.core.Local;
import org.springframework.core.io.ClassPathResource;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.joda.time.format.DateTimeFormat.forPattern;

@Component
public class MyRunner implements CommandLineRunner {

    private static final String REGEX_MATRICULE = "^[MTC][0-9]{5}$";
    private static final String REGEX_NOM = ".*";
    private static final String REGEX_PRENOM = ".*";
    private static final int NB_CHAMPS_MANAGER = 5;
    private static final int NB_CHAMPS_TECHNICIEN = 7;
    private static final String REGEX_MATRICULE_MANAGER = "^M[0-9]{5}$";
    private static final int NB_CHAMPS_COMMERCIAL = 7;

    @Autowired
    private EmployeRepository employeRepository;

    @Autowired
    private ManagerRepository managerRepository;

    private List<Employe> employes = new ArrayList<>();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void run(String... strings) throws Exception {
        String fileName = "employes.csv";
        readFile(fileName);
        //readFile(strings[0]);
    }

    /**
     * Méthode qui lit le fichier CSV en paramètre afin d'intégrer son contenu en BDD
     * @param fileName Le nom du fichier (à mettre dans src/main/resources)
     * @return une liste contenant les employés à insérer en BDD ou null si le fichier n'a pas pu être lu
     */
    public List<Employe> readFile(String fileName) {
        Stream<String> stream;
        logger.info("Lecture du fichier : " + fileName);

        try {
            stream = Files.lines(Paths.get(new ClassPathResource(fileName).getURI()));
        } catch (IOException e) {
            logger.error("Problème dans l'ouverture du fichier" + fileName);
            return  new ArrayList<>();
        }

        List<String> lignes = stream.collect(Collectors.toList());
        logger.info(lignes.size() + " lignes lues");
        for( int i = 0; i < lignes.size(); i++){
            try {
                processLine(lignes.get(i));
            } catch (BatchException e) {

                logger.error("Ligne " + (i+1) + " : " + e.getMessage() + " => " + lignes.get(i));
            }
        }
        return employes;
    }

    /**
     * Méthode qui regarde le premier caractère de la ligne et appelle la bonne méthode de création d'employé
     * @param ligne la ligne à analyser
     * @throws BatchException si le type d'employé n'a pas été reconnu
     */
    private void processLine(String ligne) throws BatchException{
        //TODO Si la 1ere lettre n'est pas c, t, m
        switch (ligne.substring(0,1)){
            case "T":
                processTechnicien(ligne);
                break;
            case "M":
                processManager(ligne);
                break;
            case "C":
                processCommercial(ligne);
                break;

             default:
                 throw new BatchException("Type d'employé inconnu");
        }

    }

    /**
     * Méthode qui crée un Commercial à partir d'une ligne contenant les informations d'un commercial et l'ajoute dans la liste globale des employés
     * @param ligneCommercial la ligne contenant les infos du commercial à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processCommercial(String ligneCommercial) throws BatchException {
        //TODO Controle le matricule

        String[] commercialFields = ligneCommercial.split(",");

        if (commercialFields.length != 7) {

            throw new BatchException("La ligne ne contient pas " + NB_CHAMPS_COMMERCIAL + " éléments mais " + commercialFields.length);

        } else if (!commercialFields[0].matches(REGEX_MATRICULE)) {

            throw new BatchException("La chaîne ne respecte pas l'expression régulière ^[MTC][0-9]{5}$ ");

        }


        else {

            Commercial c = new Commercial();
            employes.add(c);
        }

        try {
            LocalDateTime d = DateTimeFormat .forPattern("dd/MM/yyyy").parseLocalDate("")

        } 



    }

    /**
     * Méthode qui crée un Manager à partir d'une ligne contenant les informations d'un manager et l'ajoute dans la liste globale des employés
     * @param ligneManager la ligne contenant les infos du manager à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processManager(String ligneManager) throws BatchException {
        //TODO

        String[] managerFields = ligneManager.split(",");

        if (!managerFields[0].matches(REGEX_MATRICULE)) {

            throw new BatchException("La chaîne ne respecte pas l'expression régulière ^[MTC][0-9]{5}$ ");

        } else {

            Manager m = new Manager();
            employes.add(m);
        }
    }


    /**
     * Méthode qui crée un Technicien à partir d'une ligne contenant les informations d'un technicien et l'ajoute dans la liste globale des employés
     * @param ligneTechnicien la ligne contenant les infos du technicien à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processTechnicien(String ligneTechnicien) throws BatchException {
        //TODO

        String[] technicienFields = ligneTechnicien.split(",");

        if (!technicienFields[0].matches(REGEX_MATRICULE)) {
            throw new BatchException("La chaîne ne respecte pas l'expression régulière ^[MTC][0-9]{5}$ ");

        } else {

            Technicien t = new Technicien();
            employes.add(t);

        }
    }

    }

