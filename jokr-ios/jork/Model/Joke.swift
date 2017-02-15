/**
 * @name             Joke.swift
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

open class Joke: AppModelID {
    
    dynamic open var title: String = ""
    dynamic open var tags: String = ""
    dynamic open var likes: Int = 0
    dynamic open var language: String = "" // ISO 639-1 codes (en, de, fr) and "sde" swiss german see https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes
    
    dynamic open var framingBegin: Int = 1
    dynamic open var framingEnd: Int = 1
    
    dynamic open var uploadTimestamp: Int64 = 0
    
    dynamic open var userName: String = ""
    dynamic open var userId: String = ""
    
    open var localUrl: String? {
        guard let path = AppProperties.sharedInstance.documentsDirectory else {
            return nil
        }
        return (path as NSString).stringByAppendingPathComponent(modelID)
    }
    
    open var shareID: String {
        return self.modelID.stringByReplacingOccurrencesOfString("-", withString: "")
    }
    
    open var timeSinceCreation: String {
        if self.uploadTimestamp == 0 {
            let date = Date(timeIntervalSince1970: Double(self.creationTimestamp)/1000)
            return Date().offsetFrom(date)
        } else {
            let date = Date(timeIntervalSince1970: Double(self.uploadTimestamp)/1000)
            return Date().offsetFrom(date)
        }
    }
    
    open var isLocal: Bool {
        return self.uploadTimestamp == 0
    }
    
    required public init() {
       super.init()
    }
    
    public init(userId: String, userName: String?, language: String, filePath:String) {
        super.init()
        self.status = SyncStatus.NotSync.rawValue
        self.userId = userId
        self.userName = userName ?? self.userName
        self.language = language
        
        if let url = localUrl {
            let fileManager = FileManager.default
            try! fileManager.copyItem(atPath: filePath, toPath:url)
        }
    }
    
    // MARK: Mappable
    
    
    required public init?(_ map: Map) {
        super.init(map)
    }
    
    override open func mapping(_ map: Map) {
        super.mapping(map)
        title               <- map["title"]
        tags                <- map["tags"]
        likes               <- map["likes"]
        language            <- map["language"]

        framingBegin        <- map["framingBegin"]
        framingEnd          <- map["framingEnd"]

        uploadTimestamp     <- (map["uploadTimestamp"], TransformOf<Int64, NSNumber>(fromJSON: { $0?.longLongValue }, toJSON: { $0.map { NSNumber(longLong: $0) } }))

        userName            <- map["userName"]
        userId              <- map["userId"]
        modelID             <- map["_id"]
    }
}


// MARK: NSCopying

extension Joke {
    
    public override func copyWithZone(_ zone: NSZone?) -> AnyObject { // <== NSCopying
        // *** Construct "one of my current class". This is why init() is a required initializer
        let theCopy = super.copyWithZone(zone) as! Joke
        theCopy.title = self.title
        theCopy.tags = self.tags
        theCopy.likes = self.likes
        theCopy.language = self.language
        theCopy.framingBegin = self.framingBegin
        theCopy.framingEnd = self.framingEnd
        theCopy.uploadTimestamp = self.uploadTimestamp
        theCopy.userName = self.userName
        theCopy.userId = self.userId
        return theCopy
    }
}


extension Date {
    func yearsFrom(_ date:Date) -> Int{
        return (Calendar.current as NSCalendar).components(.year, from: date, to: self, options: []).year
    }
    func monthsFrom(_ date:Date) -> Int{
        return (Calendar.current as NSCalendar).components(.month, from: date, to: self, options: []).month
    }
    func weeksFrom(_ date:Date) -> Int{
        return (Calendar.current as NSCalendar).components(.weekOfYear, from: date, to: self, options: []).weekOfYear
    }
    func daysFrom(_ date:Date) -> Int{
        return (Calendar.current as NSCalendar).components(.day, from: date, to: self, options: []).day
    }
    func hoursFrom(_ date:Date) -> Int{
        return (Calendar.current as NSCalendar).components(.hour, from: date, to: self, options: []).hour
    }
    func minutesFrom(_ date:Date) -> Int{
        return (Calendar.current as NSCalendar).components(.minute, from: date, to: self, options: []).minute
    }
    func secondsFrom(_ date:Date) -> Int{
        return (Calendar.current as NSCalendar).components(.second, from: date, to: self, options: []).second
    }
    func offsetFrom(_ date:Date) -> String {
        if yearsFrom(date)   > 0 { return "\(yearsFrom(date))y"   }
        if monthsFrom(date)  > 0 { return "\(monthsFrom(date))M"  }
        if weeksFrom(date)   > 0 { return "\(weeksFrom(date))w"   }
        if daysFrom(date)    > 0 { return "\(daysFrom(date))d"    }
        if hoursFrom(date)   > 0 { return "\(hoursFrom(date))h"   }
        if minutesFrom(date) > 0 { return "\(minutesFrom(date))m" }
        if secondsFrom(date) > 0 { return "\(secondsFrom(date))s" }
        return ""
    }
}
