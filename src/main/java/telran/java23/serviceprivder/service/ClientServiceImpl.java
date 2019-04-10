package telran.java23.serviceprivder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import telran.java23.serviceprivder.configuration.AccountConfiguration;
import telran.java23.serviceprivder.dao.ClientRepository;
import telran.java23.serviceprivder.dao.ProviderRepository;
import telran.java23.serviceprivder.dao.RecordRepository;
import telran.java23.serviceprivder.dto.ClientDto;
import telran.java23.serviceprivder.dto.ClientRegisterDto;
import telran.java23.serviceprivder.dto.RecordDto;
import telran.java23.serviceprivder.dto.RecordUpdateDto;
import telran.java23.serviceprivder.exeptions.ErrorTimeException;
import telran.java23.serviceprivder.exeptions.RecordExistException;
import telran.java23.serviceprivder.exeptions.UserExistEcxeption;
import telran.java23.serviceprivder.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.stream.Collectors;

//import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class ClientServiceImpl implements ClientService {
    @Autowired
    ClientRepository clientRepository;
    @Autowired
    ClientService clientService;
    @Autowired
    ProviderRepository providerRepository;

    @Autowired
    ProviderServiceImpl providerService;
    @Autowired
    RecordRepository recordRepository;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

//    @Autowired
//    PasswordEncoder encoder;

    @Autowired
    AccountConfiguration accountConfiguration;

    @Override
    public ClientDto addNewClient(ClientRegisterDto clientDto) {
        if (clientRepository.existsById(clientDto.getEmail())) {
            throw new UserExistEcxeption("User already exist!");

        }
//        String hashPassword = encoder.encode(clientDto.getPassword());
        Client client = new Client(clientDto.getEmail(), clientDto.getPassword(), clientDto.getFirstName(), clientDto.getLastName(),
                clientDto.getTelephone());
        clientRepository.save(client);
        ClientDto clientIs = new ClientDto(clientDto.getFirstName(), clientDto.getLastName(), clientDto.getTelephone());
        return clientIs;
    }

    @Override
    public Client showProfileClient(String email) {
        clientRepository.findById(email).get();
        return clientRepository.findById(email).get();
    }

    @Override
    public ClientDto updateProfileClient(String email, ClientDto clientDto) {
        Client client = clientRepository.findById(email).get();
        client.setFirstName(clientDto.getFirstName());
        client.setLastName(clientDto.getLastName());
        client.setTelephone(clientDto.getTelephone());
        clientRepository.save(client);
        return clientDto;
    }

    public String deleteClient(String email) {
        clientRepository.deleteById(email);
        return "Client is deleted";
    }

    @Override
    public boolean login(String auth) {
        AccountUserCredential credentials = accountConfiguration.tokens(auth);
        Client client = clientRepository.findById(credentials.getLogin()).get();
        String hashPassword = credentials.getPassword();
        if (!hashPassword.equals(client.getPassword())) {
            return false;
        }

        return true;
    }

    @Override
    public Record createRecord(String emailClient, String emailProvider, RecordDto recordDto) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime dateTimeStart = LocalDateTime.parse(recordDto.getStartService(), formatter);
        LocalTime time = dateTimeStart.toLocalTime();
        LocalDate date = dateTimeStart.toLocalDate();
        Provider provider = providerRepository.findById(emailProvider).orElse(null);
        if (provider == null) {
            throw new UserExistEcxeption("Provider not exist");
        }

        Client client = clientRepository.findById(emailClient).get();

        Record record = new Record(dateTimeStart.toString(), recordDto.getService(), recordDto.getComment(),
                emailClient, emailProvider);

        LinkedHashMap<String, DayOfWeekReal> realSchedule = provider.getRealSchedule();
        DayOfWeekReal dayOfWeekReal = realSchedule.get(date.toString());
        LinkedHashMap<String, Boolean> timeRecords = dayOfWeekReal.getRealTimeRecords();

        if (timeRecords.get(time.toString()) == true) {
            LinkedHashMap<String, Record> records = provider.getRecords();
            records.put(dateTimeStart.toString(), record);
            provider.setRecords(records);

            timeRecords.put(time.toString(), false);
            dayOfWeekReal.setRealTimeRecords(timeRecords);
            realSchedule.put(date.toString(), dayOfWeekReal);
            provider.setRealSchedule(realSchedule);
            providerRepository.save(provider);
            clientRepository.save(client);

        } else {
            throw new ErrorTimeException("This time is taken");
        }

        return record;

    }

    @Override
    public Record updateRecord(String recordId, RecordUpdateDto recordDto) {
        Record record = recordRepository.findById(recordId).orElse(null);
        Record newRecord;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-d'T'HH:mm");
        LocalDateTime newRecordTime = LocalDateTime.parse(recordDto.getStartService(), formatter);
        LocalDateTime oldRecordTime = LocalDateTime.parse(record.getStartService(), formatter);
        LocalTime oldTime = oldRecordTime.toLocalTime();
        LocalDate oldDate = oldRecordTime.toLocalDate();
        LocalDate date = newRecordTime.toLocalDate();
        LocalTime time = newRecordTime.toLocalTime();
        if (newRecordTime.isBefore(LocalDateTime.now())) {
            throw new ErrorTimeException("Time is not correct");
        }
        if (record != null) {
            newRecord = updateRecordMethod(record, recordDto);
        } else {
            throw new RecordExistException("Record not exist");
        }

        Provider provider = providerRepository.findById(record.getEmailProvider()).get();
        LinkedHashMap<String, DayOfWeekReal> realSchedule = provider.getRealSchedule();
        DayOfWeekReal dayOfWeek = realSchedule.get(date.toString());
        DayOfWeekReal oldDayOfWeek = realSchedule.get(oldDate.toString());
        LinkedHashMap<String, Boolean> timeRecords = dayOfWeek.getRealTimeRecords();
        LinkedHashMap<String, Boolean> oldTimeRecords = oldDayOfWeek.getRealTimeRecords();
        if (dayOfWeek.getIsAvailable() == false) {
            throw new ErrorTimeException("This is not a working day");
        }

        if (timeRecords.get(time.toString())) {
            LinkedHashMap<String, Record> records = provider.getRecords();
            records.remove(provider.findKeyRecord(recordId));
            records.put(recordDto.getStartService(), newRecord);
            provider.setRecords(records);
            timeRecords.put(time.toString(), false);
            dayOfWeek.setRealTimeRecords(timeRecords);
            realSchedule.put(newRecordTime.toString(), dayOfWeek);

            oldTimeRecords.put(oldTime.toString(), true);
            oldDayOfWeek.setRealTimeRecords(oldTimeRecords);
            realSchedule.put(oldRecordTime.toString(), oldDayOfWeek);
            provider.setRealSchedule(realSchedule);
            providerRepository.save(provider);
        } else {
            throw new ErrorTimeException("This time is taken");

        }
        recordRepository.delete(record);
        recordRepository.save(newRecord);
        return newRecord;
    }

    public Record updateRecordMethod(Record record, RecordUpdateDto recordDto) {
        Record newRecord = new Record();
        newRecord.setStartService(recordDto.getStartService());
        newRecord.setComment(recordDto.getComment());
        newRecord.setEmailClient(record.getEmailClient());
        newRecord.setEmailProvider(record.getEmailProvider());
        newRecord.setService(record.getService());
        newRecord.setId(record.getId());
        return newRecord;
    }

    @Override
    public Record deleteRecord(String recordId) {
        Record record = recordRepository.findById(recordId).orElse(null);
            Provider provider = providerRepository.findById(record.getEmailProvider()).orElse(null);
            if (provider != null) {
                provider.deleteRecord(recordId);
                providerRepository.save(provider);
                recordRepository.deleteById(recordId);
            }
        return record;

    }


    public ClientDto clientToClientDto(Client client) {
        return new ClientDto(client.getFirstName(), client.getLastName(), client.getTelephone());
    }

    @Override
    public Double voteProvider(String email, String emailProvider, Integer vote) {
        Provider provider = providerRepository.findById(emailProvider).orElse(null);
        ArrayList<Integer> list = provider.getVote();
        list.add(vote);
        Double sum = 0.0;
        for (int i = 0; i < list.size(); i++) {
            sum = sum + list.get(i);
        }
        System.out.println(sum);
        Double vote2 = sum / list.size();
        provider.setVote(list);
        provider.setAverageVote(vote2);
        providerRepository.save(provider);
        return vote2;
    }

    @Override
    public Set<String> showAllProvidersForClient(String email) {
        return recordRepository.findAll().stream().filter((p) -> p.getEmailClient().equals(email)).map((p) -> p.getEmailProvider())
                .collect(Collectors.toSet());

    }

    @Override
    public Set<Record> showAllrecords(String email) {
        return recordRepository.findAll().stream().filter((p) -> p.getEmailClient().equals(email))
                .filter((p) -> LocalDateTime.parse(p.getStartService(), formatter).isAfter(LocalDateTime.now()))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Record> showAllrecordsForDay(String email, String date) {
        LocalDate dateForm = LocalDate.parse(date);
        HashSet<Record> recordsSet = new HashSet<>();
        Set<Record> records = recordRepository.findAll().stream().filter((p) -> p.getEmailClient().equals(email))
                .collect(Collectors.toSet());
        for (Record record : records) {
            LocalDateTime dateTime = LocalDateTime.parse(record.getStartService(), formatter);
            if (dateTime.toLocalDate().equals(dateForm)) {
                recordsSet.add(record);
            }
        }
        return recordsSet;
    }

    @Override
    public Set<Record> showArchiveRecords(String email) {
        return recordRepository.findAll().stream().filter((p) -> p.getEmailClient().equals(email))
                .filter((p) -> LocalDateTime.parse(p.getStartService(), formatter).isBefore(LocalDateTime.now()))
                .collect(Collectors.toSet());
    }
}

