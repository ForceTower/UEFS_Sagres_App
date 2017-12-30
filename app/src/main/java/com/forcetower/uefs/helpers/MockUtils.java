package com.forcetower.uefs.helpers;

import com.forcetower.uefs.database.entities.ATodoItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by João Paulo on 30/12/2017.
 */

public class MockUtils {
    public static List<ATodoItem> getTodoList() {
        List<ATodoItem> mockList = new ArrayList<>();
        mockList.add(new ATodoItem("TEC500", "Estudar bastante para a prova", "24/03/2017", true));
        mockList.add(new ATodoItem("TEC500", "Fazer a lista de exercicios", "12/08/2017", true));
        mockList.add(new ATodoItem("EXA869", "Atividade sobre Concorrência", null, false));
        return mockList;
    }
}
