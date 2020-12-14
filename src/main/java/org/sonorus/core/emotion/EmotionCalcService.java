/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sonorus.core.emotion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import org.hedwig.cloud.response.HedwigResponseCode;


import org.hedwig.leviosa.constants.CMSConstants;
import org.hedwig.cms.dto.TermInstanceDTO;
import org.leviosa.core.driver.LeviosaClientService;
import org.sonorus.core.dto.SonorusDTO;
import org.sonorus.core.dto.SonorusResultsMeta;
import org.patronus.core.client.PatronusCoreClient;
import org.patronus.constants.PatronusConstants;
import org.patronus.core.dto.FractalDTO;
import org.patronus.response.FractalResponseCode;
import org.patronus.termmeta.MFDFAResultsMeta;
import org.sonorus.core.db.DAO.EmotionDAO;
import org.sonorus.core.entities.Emohurstvalues;
import org.sonorus.core.util.DatabaseConnection;
import org.sonorus.wavread.driver.ReadAudio;



/**
 *
 * @author dgrfiv
 */
public class EmotionCalcService {

    private EntityManagerFactory emf;

    public EmotionCalcService() {
        emf = DatabaseConnection.EMF;
    }
    public SonorusDTO uploadWavToDataSeries(SonorusDTO dgrfSpeechDTO) {
        //convert wav to csv
        File fd = new File(dgrfSpeechDTO.getWavFilePath());
        ReadAudio rWAV = new ReadAudio(fd);
        try {
            rWAV.readAudioIntoText();
        } catch (IOException e) {
            dgrfSpeechDTO.setResponseCode(FractalResponseCode.DATA_SERIES_SEVERE);
            return dgrfSpeechDTO;
        }
        String csvFilePath = rWAV.getOutFname();

        
        //upload csv to dataseries table for fractal calculation
        PatronusCoreClient dss = new PatronusCoreClient();
        FractalDTO fractalDTO = new FractalDTO();
        fractalDTO.setHedwigAuthCredentials(dgrfSpeechDTO.getHedwigAuthCredentials());
        fractalDTO.setCsvFilePath(csvFilePath);
        fractalDTO.setFractalTermInstance(dgrfSpeechDTO.getSpeechDataSeriesTermInstance());
        fractalDTO = dss.uploadDataSeries(fractalDTO);
        dgrfSpeechDTO.setResponseCode(fractalDTO.getResponseCode());
        return dgrfSpeechDTO;
    }
    private SonorusDTO calculateHurst (SonorusDTO dGRFSpeechDTO) {
        //LeviosaClientService cmscs = new LeviosaClientService();
        //calculation of MFDFA
        PatronusCoreClient fractalCoreClient = new PatronusCoreClient();
        FractalDTO fractalDTO = new FractalDTO();
        fractalDTO.setHedwigAuthCredentials(dGRFSpeechDTO.getHedwigAuthCredentials());
        fractalDTO.setParamSlug("mfdfadefault");
        fractalDTO.setDataSeriesSlug(dGRFSpeechDTO.getDataSeriesSlug());

        fractalDTO = fractalCoreClient.calculateMFDFA(fractalDTO);
        if (fractalDTO.getResponseCode()!=HedwigResponseCode.SUCCESS) {
            dGRFSpeechDTO.setResponseCode(fractalDTO.getResponseCode());
            return dGRFSpeechDTO;
        }
        dGRFSpeechDTO.setMfdfaTermInstance(fractalDTO.getFractalTermInstance());
        dGRFSpeechDTO.setResponseCode(HedwigResponseCode.SUCCESS);
        return dGRFSpeechDTO;
    }
    private List<Emohurstvalues> getBaseEmotionValues() {
        EmotionDAO emotionDAO = new EmotionDAO(emf);
        List<Emohurstvalues> baseEmoHurstvalues = emotionDAO.findEmohurstvaluesEntities();
        return baseEmoHurstvalues;
    }
    private List<Emohurstvalues> getAverageEmoHusrtValues() {
        EmotionDAO emotionDAO = new EmotionDAO(emf);
        List avgHurstEmoList = emotionDAO.getAvgEmohurst();
        if (avgHurstEmoList == null) {
            return null;
        }
        List<Emohurstvalues> averageEmoHusrtValues = new ArrayList<>();
        for (Object obj : avgHurstEmoList) {
            Emohurstvalues averageEmoHusrtValue = new Emohurstvalues();
            Object[] row = (Object[]) obj;
            Double hurstvalue = (Double) row[0];
            String emotion = (String) row[1];
            String subjectType = (String)row[2];
            averageEmoHusrtValue.setEmotion(emotion);
            averageEmoHusrtValue.setSubjectType(subjectType);
            averageEmoHusrtValue.setHurstValue(hurstvalue);
            averageEmoHusrtValues.add(averageEmoHusrtValue);
            
        }
        return averageEmoHusrtValues;
    }
    private SonorusDTO createSpeechEmoTermInstance (List<String> decidedEmotions,SonorusDTO dgrfSpeechDTO) {
        String mfdfaTermInstanceSlug = (String) dgrfSpeechDTO.getMfdfaTermInstance().get(CMSConstants.TERM_INSTANCE_SLUG);
        Map<String, Object> speechEmoTermInstance = new HashMap<>();
        speechEmoTermInstance.put(SonorusResultsMeta.DATASERIES, dgrfSpeechDTO.getDataSeriesSlug());
        speechEmoTermInstance.put(SonorusResultsMeta.EMOTION, decidedEmotions);
        speechEmoTermInstance.put(SonorusResultsMeta.MFDFA_INSTANCE, mfdfaTermInstanceSlug);
        speechEmoTermInstance.put(CMSConstants.TERM_INSTANCE_SLUG, mfdfaTermInstanceSlug + "emo");
        speechEmoTermInstance.put(CMSConstants.TERM_SLUG, PatronusConstants.TERM_SLUG_SPEECH_EMO);
        dgrfSpeechDTO.setSpeechEmoTermInstance(speechEmoTermInstance);
        return dgrfSpeechDTO;
    }
    private SonorusDTO  saveEmotionToCMS(SonorusDTO sonorusDTO) {
        

        TermInstanceDTO termInstanceDTO = new TermInstanceDTO();
        termInstanceDTO.setHedwigAuthCredentials(sonorusDTO.getHedwigAuthCredentials());
        termInstanceDTO.setTermSlug(PatronusConstants.TERM_SLUG_SPEECH_EMO);
        termInstanceDTO.setTermInstance(sonorusDTO.getSpeechEmoTermInstance());
        LeviosaClientService cmscs = new LeviosaClientService(sonorusDTO.getHedwigAuthCredentials().getHedwigServer(),sonorusDTO.getHedwigAuthCredentials().getHedwigServerPort());
        termInstanceDTO = cmscs.saveTermInstance(termInstanceDTO);
        if (termInstanceDTO.getResponseCode()!=HedwigResponseCode.SUCCESS) {
            sonorusDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return sonorusDTO;
        }
        
        sonorusDTO.setResponseCode(HedwigResponseCode.SUCCESS);
        return sonorusDTO;
        
    }
    public SonorusDTO decideEmotion(SonorusDTO sonorusDTO) {
        //get data series term instance
        LeviosaClientService cmscs = new LeviosaClientService(sonorusDTO.getHedwigAuthCredentials().getHedwigServer(),sonorusDTO.getHedwigAuthCredentials().getHedwigServerPort());
        TermInstanceDTO termInstanceDTO = new TermInstanceDTO();
        termInstanceDTO.setHedwigAuthCredentials(sonorusDTO.getHedwigAuthCredentials());
        termInstanceDTO.setTermSlug(PatronusConstants.TERM_SLUG_DATASERIES);
        termInstanceDTO.setTermInstanceSlug(sonorusDTO.getDataSeriesSlug());
        termInstanceDTO = cmscs.getTermInstance(termInstanceDTO);
        
        if ( termInstanceDTO.getResponseCode() != HedwigResponseCode.SUCCESS) {
            sonorusDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return sonorusDTO;
        }
        sonorusDTO.setSpeechDataSeriesTermInstance(termInstanceDTO.getTermInstance());
        //calculate MFDFA of data series
        sonorusDTO = calculateHurst(sonorusDTO);
        
        if (sonorusDTO.getResponseCode() != HedwigResponseCode.SUCCESS) {
            return sonorusDTO;
        }
        String hurstexponent = (String) sonorusDTO.getMfdfaTermInstance().get(MFDFAResultsMeta.HURST_EXPONENT);
        //calculate emotion
        
        List<String> decidedEmotions = calculateEmo(hurstexponent);
        //
        sonorusDTO = createSpeechEmoTermInstance(decidedEmotions,sonorusDTO);
        //save emotion term instance
        sonorusDTO = saveEmotionToCMS(sonorusDTO);
        

        return sonorusDTO;
    }   
    private List<String>  calculateEmo (String hurstexponent) {
        //getting average hurst value for different emotions
        List<Emohurstvalues> averageEmoHusrtValues = getAverageEmoHusrtValues();
        if (averageEmoHusrtValues == null) {
            return null;
        }
        //compare hurst exponent with base values
        
        Double sampleHurstExponent = Double.valueOf(hurstexponent);
        
        List<String> decidedEmotions = new ArrayList<>();
        for (Emohurstvalues emohurstvalues: averageEmoHusrtValues) {
            if (emohurstvalues.getHurstValue()> sampleHurstExponent - 0.05 && emohurstvalues.getHurstValue() < sampleHurstExponent + 0.05) {
                if (!decidedEmotions.contains(emohurstvalues.getEmotion()))
                decidedEmotions.add(emohurstvalues.getEmotion());
            }
        }
        if (decidedEmotions.isEmpty()) {
            //check if it is lower than the lowest average emotion
            if (sampleHurstExponent < averageEmoHusrtValues.get(0).getHurstValue()) {
                decidedEmotions.add(averageEmoHusrtValues.get(0).getEmotion());
            } else if (sampleHurstExponent > averageEmoHusrtValues.get(averageEmoHusrtValues.size()-1).getHurstValue()){
            //check if it is higher than the highest average emotion    
                decidedEmotions.add(averageEmoHusrtValues.get(averageEmoHusrtValues.size()-1).getEmotion());
            } else {
                decidedEmotions.add("notidentified");
            }
        }
        return decidedEmotions;
    }


    public SonorusDTO deleteSpeechEmoInstance(SonorusDTO sonorusDTO) {
        
        PatronusCoreClient fractalCoreClient = new PatronusCoreClient();
        Map<String, Object> fractalTermInstance = new HashMap<>();
        fractalTermInstance.put(CMSConstants.TERM_SLUG, PatronusConstants.TERM_SLUG_MFDFA_CALC);
        fractalTermInstance.put(CMSConstants.TERM_INSTANCE_SLUG, sonorusDTO.getSpeechEmoTermInstance().get(SonorusResultsMeta.MFDFA_INSTANCE));
        FractalDTO fractalDTO = new FractalDTO();
        
        fractalDTO.setHedwigAuthCredentials(sonorusDTO.getHedwigAuthCredentials());
        fractalDTO.setFractalTermInstance(fractalTermInstance);

        fractalDTO = fractalCoreClient.deleteMFDFAResults(fractalDTO);
        
        if ( fractalDTO.getResponseCode() != HedwigResponseCode.SUCCESS) {
            sonorusDTO.setResponseCode(fractalDTO.getResponseCode());
            return sonorusDTO;
        }
        
        String selectedTermInstanceSlug = (String) sonorusDTO.getSpeechEmoTermInstance().get(CMSConstants.TERM_INSTANCE_SLUG);
        LeviosaClientService cmscs = new LeviosaClientService(sonorusDTO.getHedwigAuthCredentials().getHedwigServer(),sonorusDTO.getHedwigAuthCredentials().getHedwigServerPort());
        
        TermInstanceDTO termInstanceDTO = new TermInstanceDTO();
        termInstanceDTO.setHedwigAuthCredentials(sonorusDTO.getHedwigAuthCredentials());
        termInstanceDTO.setTermSlug((String) sonorusDTO.getSpeechEmoTermInstance().get(CMSConstants.TERM_SLUG));
        termInstanceDTO.setTermInstanceSlug(selectedTermInstanceSlug);
        termInstanceDTO = cmscs.deleteTermInstance(termInstanceDTO);
        
        if ( termInstanceDTO.getResponseCode() != HedwigResponseCode.SUCCESS) {
            sonorusDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return sonorusDTO;
        }
        
        sonorusDTO.setResponseCode(HedwigResponseCode.SUCCESS);
        return sonorusDTO;
    }

}
