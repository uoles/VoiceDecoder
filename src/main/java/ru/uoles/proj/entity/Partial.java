package ru.uoles.proj.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

public class Partial {

    private String partial;

    @JsonIgnore
    private String text;

    public Partial() {
    }

    public Partial(String partial, String text) {
        this.partial = partial;
        this.text = text;
    }

    public String getPartial() {
        return partial;
    }

    public void setPartial(String partial) {
        this.partial = partial;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Partial)) return false;
        Partial partial1 = (Partial) o;
        return Objects.equals(getPartial(), partial1.getPartial()) && Objects.equals(getText(), partial1.getText());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPartial(), getText());
    }
}
