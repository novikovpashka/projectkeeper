package com.example.projectkeeper.domain.usecases

import com.example.projectkeeper.domain.models.EmailAndPasswordParam

class LoginByEmailUseCase {
    fun execute(EmailAndPasswordParam: EmailAndPasswordParam) : Boolean {
        return EmailAndPasswordParam.email.isNotEmpty() && EmailAndPasswordParam.password.isNotEmpty()
    }
}