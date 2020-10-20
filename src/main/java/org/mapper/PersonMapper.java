package org.mapper;

import org.entity.Person;
import java.util.List;

public interface PersonMapper {

    int deletePersonById(Integer id);

    Person selectPersonById(Integer id);

    List<Person> selectPersonsByName(String name);

    List<Person> selectPersonsByAge(Integer age);

    List<Person> selectPersonsByHomeAddress(String homeAddress);

    List<Person> selectPersonsByCompanyAddress(String companyAddress);

    int insertPerson(Person person);

    int updatePerson(Person person);

}
