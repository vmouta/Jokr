/**
 * @name             User.swift
 * @partof           Jokr
 * @description
 * @author	 		Vasco Mouta
 * @created			28/12/15
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

open class User: AppUser {
    
    static let DefaultLanguage = "en"
    static let UserImageName = "userlogo.png"
    static let LanguageSplitChar = " "
    
    dynamic open var serverID: String?
    
    dynamic open var deviceId: String = ""
    dynamic open var deviceOS: String = "i"   // a: android, i: ios
    dynamic open var languages: String?   // languages, comma separeted
    
    dynamic open var numberOfFollowers: Int = 0
    dynamic open var jokesCnt:Int = 0
    
    dynamic open var listeningTimestamp: Int64 = 0

    required public init() {
        super.init()
    }
    
    public init(deviceID: String, languages: String? = " sde de en fr it es pt ") {
        super.init()
        self.deviceId = deviceID
        self.languages = languages
    }
    
    open func getLangs() -> [String] {
        guard let langs = languages else {
            return []
        }
        return langs.components(separatedBy: User.LanguageSplitChar)
    }
    
    open func setLangs(_ languages: [String]) {
        var langs: String? = nil
        for lang in languages {
            if langs != nil {
                langs = langs! + User.LanguageSplitChar + lang
            } else {
                langs = lang
            }
        }
        self.languages = langs
    }
    
    open func serverDictionary() -> [String:AnyObject] {
        return self.toSpecificDictionary(["_id", "userName", "deviceId", "deviceOS", "languages", "creationTimestamp", "lastActiveTimestamp", "listeningTimestamp"])
    }
    
    // MARK: Mappable
    
    required public init?(_ map: Map) {
        super.init(map)
    }
    
    override open func mapping(_ map: Map) {
        super.mapping(map)
        deviceId            <- map["deviceId"]
        deviceOS            <- map["deviceOS"]
        languages           <- map["languages"]
        numberOfFollowers   <- map["followersCnt"]
        listeningTimestamp  <- map["listeningTimestamp"]
        jokesCnt            <- map["jokesCnt"]
        serverID            <- map["serverID"]
        serverID            <- map["_id"]
    }
}


// MARK: NSCopying

extension User {
    
    public override func copyWithZone(_ zone: NSZone?) -> AnyObject { // <== NSCopying
        // *** Construct "one of my current class". This is why init() is a required initializer
        let theCopy = super.copyWithZone(zone) as! User
        theCopy.deviceId = self.deviceId
        theCopy.deviceOS = self.deviceOS
        theCopy.languages = self.languages
        theCopy.serverID = self.serverID
        theCopy.numberOfFollowers = self.numberOfFollowers
        theCopy.jokesCnt = self.jokesCnt
        theCopy.listeningTimestamp = self.listeningTimestamp
        return theCopy
    }
}
