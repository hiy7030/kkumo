package com.kkumo.domain.reaction.repository

import com.kkumo.domain.reaction.Reaction
import org.springframework.data.jpa.repository.JpaRepository

interface ReactionRepository : JpaRepository<Reaction, Long>