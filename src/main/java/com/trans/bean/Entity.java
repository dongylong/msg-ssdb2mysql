// tag::sample[]
package com.trans.bean;

import javax.persistence.*;

/**
 * @author dongyl
 */
@Entity
@Table(name = "entity")
public class Entity {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private  long id;
    private  String content;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Entity{" +
                "id=" + id +
                ", content='" + content + '\'' +
                '}';
    }
}

