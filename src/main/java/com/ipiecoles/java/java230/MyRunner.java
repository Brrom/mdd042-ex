package com.ipiecoles.java.java230;

import com.ipiecoles.java.java230.exceptions.BatchException;
import com.ipiecoles.java.java230.exceptions.TechnicienException;
import com.ipiecoles.java.java230.model.Commercial;
import com.ipiecoles.java.java230.model.Employe;
import com.ipiecoles.java.java230.model.Manager;
import com.ipiecoles.java.java230.model.Technicien;
import com.ipiecoles.java.java230.repository.EmployeRepository;
import com.ipiecoles.java.java230.repository.ManagerRepository;

import org.joda.time.LocalDate;

import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cglib.core.Local;
import org.springframework.core.io.ClassPathResource;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
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
        employeRepository.save(employes);
    }

    /**
     * Méthode qui lit le fichier CSV en paramètre afin d'intégrer son contenu en BDD
     *
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
            return new ArrayList<>();
        }

        List<String> lignes = stream.collect(Collectors.toList());
        logger.info(lignes.size() + " lignes lues");
        for (int i = 0; i < lignes.size(); i++) {
            try {
                processLine(lignes.get(i));
            } catch (BatchException e) {
                logger.error("Ligne " + (i + 1) + " : " + e.getMessage() + " => " + lignes.get(i));
            }
        }
        return employes;
    }

    /**
     * Méthode qui regarde le premier caractère de la ligne et appelle la bonne méthode de création d'employé
     *
     * @param ligne la ligne à analyser
     * @throws BatchException si le type d'employé n'a pas été reconnu
     */
    private void processLine(String ligne) throws BatchException {
        //TODO
        //Si la 1ere lettre n'est pas C, T, M
        switch (ligne.substring(0, 1)) {
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

    private void processEmploye(Employe employe, String ligneEmploye, String regexMatricule) throws BatchException {
        String[] employeFields = ligneEmploye.split(",");

        //Controle matricule employe
        if (!employeFields[0].matches(regexMatricule)) {
            throw new BatchException(employeFields[0] + " La chaîne ne respecte pas l'expression régulière " + regexMatricule);
        }

        //Controle date employe
        LocalDate d = null;
        try {
            d = DateTimeFormat.forPattern("dd/MM/yyyy").parseLocalDate(employeFields[3]);
        } catch (Exception e) {
            throw new BatchException(employeFields[3] + " ne respecte pas le format de date dd/MM/yyyy ");
        }

        //Controle salaire employe
        try {
            float salaire = Float.parseFloat(employeFields[4]);
        } catch (NumberFormatException e) {
            throw new BatchException(employeFields[4] + " n'est pas un nombre valide pour un salaire ");
        }

        employe.setMatricule(employeFields[0]);
        employe.setNom(employeFields[1]);
        employe.setPrenom(employeFields[2]);
        employe.setDateEmbauche(d);
        employe.setSalaire(Double.parseDouble(employeFields[4]));
    }


    /**
     * Méthode qui crée un Commercial à partir d'une ligne contenant les informations d'un commercial et l'ajoute dans la liste globale des employés
     *
     * @param ligneCommercial la ligne contenant les infos du commercial à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processCommercial(String ligneCommercial) throws BatchException {
        //TODO Controle le matricule

        String[] commercialFields = ligneCommercial.split(",");

        Commercial c = new Commercial();

        //Controle nombre de champs
        if (commercialFields.length != NB_CHAMPS_COMMERCIAL) {
            throw new BatchException("La ligne ne contient pas " + NB_CHAMPS_COMMERCIAL + " éléments mais " + commercialFields.length);
        }

        //Controle chiffre d'affaire commercial
        try {
            float chiffreAffaire = Float.parseFloat(commercialFields[5]);
        } catch (NumberFormatException e) {
            throw new BatchException("Le chiffre d'affaire du commercial est incorrect : " + commercialFields[5]);
        }

        //Controle performance commercial
        try {
            float performance = Float.parseFloat(commercialFields[6]);
        } catch (NumberFormatException e) {
            throw new BatchException("La performance du commercial est incorrecte : " + commercialFields[6]);
        }

        processEmploye(c,ligneCommercial,REGEX_MATRICULE);

        c.setCaAnnuel(Double.parseDouble(commercialFields[5]));
        c.setPerformance(Integer.parseInt(commercialFields[6]));

        employes.add(c);
    }


    /**
     * Méthode qui crée un Manager à partir d'une ligne contenant les informations d'un manager et l'ajoute dans la liste globale des employés
     *
     * @param ligneManager la ligne contenant les infos du manager à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processManager(String ligneManager) throws BatchException {
        //TODO
        String[] managerFields = ligneManager.split(",");

        Manager m = new Manager();

        //Controle nombre de champs
        if (managerFields.length != NB_CHAMPS_MANAGER) {
            throw new BatchException("La ligne ne contient pas " + NB_CHAMPS_MANAGER + " éléments mais " + managerFields.length);
        }

        processEmploye(m,ligneManager,REGEX_MATRICULE_MANAGER);

        employes.add(m);
    }


    /**
     * Méthode qui crée un Technicien à partir d'une ligne contenant les informations d'un technicien et l'ajoute dans la liste globale des employés
     *
     * @param ligneTechnicien la ligne contenant les infos du technicien à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processTechnicien(String ligneTechnicien) throws BatchException {
        //TODO

        String[] TechnicienFields = ligneTechnicien.split(",");

        Technicien t = new Technicien();

        //Controle nombre de champs
        if (TechnicienFields.length != NB_CHAMPS_TECHNICIEN) {
            throw new BatchException("La ligne ne contient pas " + NB_CHAMPS_TECHNICIEN + " éléments mais " + TechnicienFields.length);
        }

        //Controle grade technicien
        int grade;
        try {
            grade = Integer.parseInt(TechnicienFields[5]);
        } catch (Exception e) {
            throw new BatchException("Le grade du technicien est incorrect");
        }

        //Controle matricule manager
        if (!TechnicienFields[6].matches(REGEX_MATRICULE_MANAGER)) {
            throw new BatchException("la chaîne " + TechnicienFields[6] + " ne respecte pas l'expression régulière " + REGEX_MATRICULE_MANAGER);
        }

        ArrayList<String> list = new ArrayList<>();
        for (Employe e : employes){
            if (e instanceof Manager) {
                list.add(e.getMatricule());
            }
        }

        //Controle si matricule manager n'existe pas dans la bdd
        if (managerRepository.findByMatricule(TechnicienFields[6]) == null && !list.contains(TechnicienFields[6])){
            throw new BatchException ("Le manager de matricule " + TechnicienFields[6] + " n'a pas été trouvé dans le fichier ou en base de données");
        }

        //Controle grade technicien
        try {
            t.setGrade(Integer.parseInt(TechnicienFields[5]));
        }catch (TechnicienException e ){
            throw  new BatchException("Le grade doit être compris entre 1 et 5");
        }

        processEmploye(t,ligneTechnicien,REGEX_MATRICULE);

        employes.add(t);
    }
}




