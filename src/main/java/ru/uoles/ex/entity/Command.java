package ru.uoles.ex.entity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

public class Command {

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private String partial;
    private String dataTime;
    private String result;

    public Command() {
    }

    public Command(String partial, String result) {
        this.partial = partial;
        this.result = result;
        this.dataTime = simpleDateFormat.format(Calendar.getInstance().getTime());
    }

    public String getPartial() {
        return partial;
    }

    public String getDataTime() {
        return dataTime;
    }

    public String getResult() {
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Command)) return false;
        Command that = (Command) o;
        return Objects.equals(getPartial(), that.getPartial()) && Objects.equals(getDataTime(), that.getDataTime()) && Objects.equals(getResult(), that.getResult());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPartial(), getDataTime(), getResult());
    }

    @Override
    public String toString() {
        return "PartialJournal { " +
                "partial='" + partial + '\'' +
                ", dataTime='" + dataTime + '\'' +
                ", result='" + result + '\'' +
                " }";
    }
}
