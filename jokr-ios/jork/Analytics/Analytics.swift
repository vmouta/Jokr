/**
* @name             Analytics.swift
* @partof           zucred AG
* @description
* @author	 		Vasco Mouta
* @created			20/11/15
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
import Google

open class Analytics: NSObject {
    
    internal let logger = AppLogger.logger(Analytics.fullName())
    internal let analiticsInstance = GAI.sharedInstance()
    
    open static let sharedInstance : Analytics = {
        let instance = Analytics()
        
        // Configure tracker from GoogleService-Info.plist.
        var configureError:NSError?
        GGLContext.sharedInstance().configureWithError(&configureError)
        assert(configureError == nil, "Error configuring Google services: \(configureError)")
        
        return instance
    }()
    
    override init() {
        super.init()
        logger.debug()
        
        analiticsInstance.trackUncaughtExceptions = true  // report uncaught exceptions
        //analiticsInstance.logger.logLevel = GAILogLevel.Verbose  // remove before app release
        
        let notificationCenter = NotificationCenter.default
        notificationCenter.addObserver(self, selector: "appMovedToBackground", name: UIApplicationDidEnterBackgroundNotification, object: nil)
        notificationCenter.addObserver(self, selector: "appMovedToForeground", name: UIApplicationWillEnterForegroundNotification, object: nil)
        notificationCenter.addObserver(self, selector: "appTerminate", name: UIApplicationWillTerminateNotification, object: nil)
        notificationCenter.addObserver(self, selector: "appResignActive", name: UIApplicationWillResignActiveNotification, object: nil)
        notificationCenter.addObserver(self, selector: "appBecomeActive", name: UIApplicationDidBecomeActiveNotification, object: nil)
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    /*!
    Returns a GAIDictionaryBuilder object with parameters specific to a screenview
    hit.
    
    Note that using this method will not set the screen name for followon hits.  To
    do that you need to call set:kGAIDescription value:<screenName> on the
    GAITracker instance.
    */
    open func createScreenView(_ name: String) {
        let builder = GAIDictionaryBuilder.createScreenView()
        let tracker = GAI.sharedInstance().defaultTracker
        tracker.set(kGAIScreenName, value: name)
        tracker.send(builder.build() as [AnyHashable: Any])
    }
    
    /**
    Returns a GAIDictionaryBuilder object with parameters specific to an event hit.
    
    - parameter category: <#category description#>
    - parameter action:   <#action description#>
    - parameter label:    <#label description#>
    - parameter value:    <#value description#>
    */
    open func trackCustomEvent(_ category: String!, action: String!, label: String!, value: NSNumber!) {
        let event = GAIDictionaryBuilder.createEventWithCategory(category, action: action, label: label, value: value)
        let tracker = analiticsInstance.defaultTracker
        tracker.send(event.build() as [AnyHashable: Any])
    }
    
    // MARK: NSNotificationCenter notifications
    
    open func appMovedToBackground() {

    }
    
    open func appMovedToForeground() {

    }
    
    open func appTerminate() {

    }
    
    open func appResignActive() {

    }
    
    open func appBecomeActive() {

    }
}
