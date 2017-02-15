/**
* @name             TrackUICollectionViewController.swift
* @partof           Analytics
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

import UIKit

open class TrackUICollectionViewController: UICollectionViewController {
        
    open override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(true)
        Analytics.sharedInstance.createScreenView(self.className())
        Analytics.sharedInstance.logger.verbose("TrackUICollectionViewController willAppear \(self.className())")
    }
}
