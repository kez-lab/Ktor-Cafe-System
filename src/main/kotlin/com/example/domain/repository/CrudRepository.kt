package com.example.domain.repository

import com.example.domain.model.BaseModel

interface CrudRepository<DOMAIN : BaseModel> {
    fun create(domain: DOMAIN): DOMAIN
    fun findAll(): List<DOMAIN>
    fun read(id: Long): DOMAIN?
    fun update(domain: DOMAIN): DOMAIN
    fun delete(domain: DOMAIN)
    fun delete(id: Long)
}