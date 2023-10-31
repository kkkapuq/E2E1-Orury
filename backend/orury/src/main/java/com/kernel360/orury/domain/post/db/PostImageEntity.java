package com.kernel360.orury.domain.post.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kernel360.orury.global.domain.BaseEntity;
import com.kernel360.orury.domain.post.db.PostEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@SuperBuilder
@Entity(name = "post_image")
public class PostImageEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    private PostEntity post;

    private String imageUrl;
}