package com.novikovpashka.projectkeeper.domain.usecases

import com.novikovpashka.projectkeeper.domain.models.EmailAndPasswordParam

class LoginByEmailUseCase {
    fun execute(EmailAndPasswordParam: EmailAndPasswordParam) : Boolean {
        return EmailAndPasswordParam.email.isNotEmpty() && EmailAndPasswordParam.password.isNotEmpty()
    }
}