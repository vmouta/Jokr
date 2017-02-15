/**
* @name             JokrDB.swift
* @partof           zucred AG
* @description
* @author	 		Vasco Mouta
* @created			9/01/15
*
* Copyright (c) 2015 zucred AG All rights reserved.
* This material, including documentation and any related
* computer programs, is protected by copyright controlled by
* zucred AG. All rights are reserved. Copying,
* including reproducing, storing, adapting or translating, any
* or all of this material requires the prior written consent of
* zucred AG. This material also contains confidential
* information which may not be disclosed to others without the
* prior written consent of zucred AG.
*/

import Foundation
import zucredApple
import ObjectMapper

open class JokrDB: AppDB {
    
    open override class var sharedInstance: JokrDB {
        return super.sharedInstance as! JokrDB
    }
    
    open override func factory(_ type: String, values: [String: AnyObject]) -> AppModel? {
        var object:AppModel?
        if(type == "Joke") {
            object = Mapper<Joke>().map(values)
        } else if(type == "User") {
            object = Mapper<User>().map(values)
        } else if(type == "Login") {
            object = Mapper<Login>().map(values)
        } else {
            object = super.factory(type, values: values)
        }
        return object
    }
}
